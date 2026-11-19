package com.spark.test.llm;

import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.base.SessionHolder;
import com.spark.common.bean.llm.dto.ModelInfo;
import com.spark.common.bean.llm.dto.UsageSnapshot;
import com.spark.common.bean.sys.entity.Session;
import com.spark.common.enums.AccountTypeEnum;
import com.spark.common.enums.CircuitStateEnum;
import com.spark.common.enums.ModelErrorTypeEnum;
import com.spark.common.enums.TenantConfigEnum;
import com.spark.llm.model.circuit.ProviderCircuitBreaker;
import com.spark.llm.model.error.ModelInvocationException;
import com.spark.llm.model.error.ModelRetryPolicy;
import com.spark.llm.model.fallback.FallbackChatModel;
import com.spark.llm.model.fallback.FallbackEmbeddingModel;
import com.spark.llm.model.fallback.FallbackModelTarget;
import com.spark.llm.model.fallback.FallbackScoringModel;
import com.spark.llm.model.fallback.FallbackStreamingChatModel;
import com.spark.llm.model.invocation.ModelInvoker;
import com.spark.llm.model.ratelimit.TenantRateLimiter;
import com.spark.llm.usage.ModelUsageRecorder;
import com.spark.manage.sys.ITenantConfigService;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.exception.HttpException;
import dev.langchain4j.model.ModelProvider;
import dev.langchain4j.model.chat.Capability;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.request.DefaultChatRequestParameters;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.model.output.TokenUsage;
import dev.langchain4j.model.scoring.ScoringModel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongSupplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-11-19 16:40:00
 * 四类模型外观租户限流接入测试
 */
public class ModelRateLimitFallbackTest {
    private ModelInvoker modelInvoker;
    private ProviderCircuitBreaker circuitBreaker;
    private TenantRateLimiter rateLimiter;
    private ITenantConfigService tenantConfigService;
    private ModelUsageRecorder usageRecorder;
    private AtomicLong nowMillis;

    @BeforeEach
    void setUp() {
        ScheduledThreadPoolExecutor scheduler = mock(ScheduledThreadPoolExecutor.class);
        ExecutorService callbackExecutor = mock(ExecutorService.class);
        doAnswer(invocation -> {
            Runnable task = invocation.getArgument(0);
            task.run();
            return null;
        }).when(callbackExecutor).execute(any(Runnable.class));
        ModelRetryPolicy retryPolicy = new ModelRetryPolicy(() -> 0);
        modelInvoker = new ModelInvoker(scheduler, callbackExecutor, retryPolicy);
        circuitBreaker = new ProviderCircuitBreaker();
        rateLimiter = new TenantRateLimiter();
        tenantConfigService = mock(ITenantConfigService.class);
        ReflectionTestUtils.setField(rateLimiter, "tenantConfigService", tenantConfigService);
        nowMillis = new AtomicLong(0L);
        LongSupplier clock = nowMillis::get;
        ReflectionTestUtils.setField(rateLimiter, "clock", clock);
        usageRecorder = mock(ModelUsageRecorder.class);
        Session session = new Session();
        session.setUserId(201L);
        session.setTenantId(103L);
        session.setAccountType(AccountTypeEnum.COMMON.getValue());
        SessionHolder.initLocalSession(session);
    }

    @AfterEach
    void tearDown() {
        modelInvoker.destroy();
        SessionHolder.clearLocalSession();
    }

    /**
     * 验证RPM=1时第二次聊天在外观入口被拒绝抛RATE_LIMIT与429，底层模型只调用一次，熔断保持闭合且记录一条限流用量
     */
    @Test
    void shouldRejectChatWithRateLimitAndKeepCircuitClosed() {
        this.stubLimits("0", "1", "0");
        ModelInfo primaryInfo = new ModelInfo(1L, "primary", 11L, 1);
        ChatModel primary = this.prepareChatModel(mock(ChatModel.class));
        ChatResponse successResponse = mock(ChatResponse.class);
        when(primary.doChat(any(ChatRequest.class))).thenReturn(successResponse);
        FallbackModelTarget<ChatModel> primaryTarget = this.createChatTarget(primaryInfo, primary);
        FallbackChatModel model = new FallbackChatModel(List.of(primaryTarget), List.of(), circuitBreaker, rateLimiter, usageRecorder);
        ChatRequest request = this.createRequest();
        ChatResponse response = model.chat(request);
        assertSame(successResponse, response);
        ModelInvocationException exception = assertThrows(ModelInvocationException.class, () -> model.chat(request));
        assertEquals(ModelErrorTypeEnum.RATE_LIMIT, exception.getErrorType());
        assertEquals(429, exception.getHttpStatus());
        assertEquals("tenant rate limit exceeded", exception.getMessage());
        verify(primary, times(1)).doChat(any(ChatRequest.class));
        assertEquals(CircuitStateEnum.CLOSED, circuitBreaker.getState(11L));
        verify(usageRecorder).recordRateLimited(eq(primaryInfo), any(UsageSnapshot.class));
    }

    /**
     * 验证并发限额为1时两次顺序聊天均成功，每次调用结束释放并发许可不发生误拒
     */
    @Test
    void shouldAllowSequentialChatWhenConcurrentOne() {
        this.stubLimits("1", "0", "0");
        ModelInfo primaryInfo = new ModelInfo(1L, "primary", 11L, 1);
        ChatModel primary = this.prepareChatModel(mock(ChatModel.class));
        ChatResponse successResponse = mock(ChatResponse.class);
        when(primary.doChat(any(ChatRequest.class))).thenReturn(successResponse);
        FallbackModelTarget<ChatModel> primaryTarget = this.createChatTarget(primaryInfo, primary);
        FallbackChatModel model = new FallbackChatModel(List.of(primaryTarget), List.of(), circuitBreaker, rateLimiter, usageRecorder);
        ChatRequest request = this.createRequest();
        ChatResponse first = model.chat(request);
        assertSame(successResponse, first);
        ChatResponse second = model.chat(request);
        assertSame(successResponse, second);
        verify(primary, times(2)).doChat(any(ChatRequest.class));
    }

    /**
     * 验证RPM=1时第二次向量化在入口被拒绝抛RATE_LIMIT，底层向量模型只调用一次并记录限流用量
     */
    @Test
    void shouldRejectEmbeddingWithRateLimit() {
        this.stubLimits("0", "1", "0");
        ModelInfo primaryInfo = new ModelInfo(1L, "primary", 11L, 2);
        EmbeddingModel primary = mock(EmbeddingModel.class);
        List<TextSegment> segments = List.of(TextSegment.from("text"));
        Response<List<Embedding>> successResponse = new Response<>(List.of());
        when(primary.embedAll(segments)).thenReturn(successResponse);
        FallbackModelTarget<EmbeddingModel> primaryTarget = this.createEmbeddingTarget(primaryInfo, primary);
        FallbackEmbeddingModel model = new FallbackEmbeddingModel(List.of(primaryTarget), circuitBreaker, rateLimiter, usageRecorder);
        Response<List<Embedding>> response = model.embedAll(segments);
        assertSame(successResponse, response);
        ModelInvocationException exception = assertThrows(ModelInvocationException.class, () -> model.embedAll(segments));
        assertEquals(ModelErrorTypeEnum.RATE_LIMIT, exception.getErrorType());
        assertEquals(429, exception.getHttpStatus());
        verify(primary, times(1)).embedAll(segments);
        verify(usageRecorder).recordRateLimited(eq(primaryInfo), any(UsageSnapshot.class));
    }

    /**
     * 验证RPM=1时第二次排序在入口被拒绝抛RATE_LIMIT，底层排序模型只调用一次并记录限流用量
     */
    @Test
    void shouldRejectScoringWithRateLimit() {
        this.stubLimits("0", "1", "0");
        ModelInfo primaryInfo = new ModelInfo(1L, "primary", 11L, 3);
        ScoringModel primary = mock(ScoringModel.class);
        List<TextSegment> segments = List.of(TextSegment.from("text"));
        Response<List<Double>> successResponse = new Response<>(List.of(1.0));
        when(primary.scoreAll(segments, "query")).thenReturn(successResponse);
        FallbackModelTarget<ScoringModel> primaryTarget = this.createScoringTarget(primaryInfo, primary);
        FallbackScoringModel model = new FallbackScoringModel(List.of(primaryTarget), circuitBreaker, rateLimiter, usageRecorder);
        Response<List<Double>> response = model.scoreAll(segments, "query");
        assertSame(successResponse, response);
        ModelInvocationException exception = assertThrows(ModelInvocationException.class,
                () -> model.scoreAll(segments, "query"));
        assertEquals(ModelErrorTypeEnum.RATE_LIMIT, exception.getErrorType());
        assertEquals(429, exception.getHttpStatus());
        verify(primary, times(1)).scoreAll(segments, "query");
        verify(usageRecorder).recordRateLimited(eq(primaryInfo), any(UsageSnapshot.class));
    }

    /**
     * 验证RPM=1时第二次流式聊天在入口被拒绝，业务handler收到RATE_LIMIT异常而非抛出，底层模型只调用一次并记录限流用量
     */
    @Test
    void shouldRejectStreamingWithRateLimitViaHandler() {
        this.stubLimits("0", "1", "0");
        ModelInfo primaryInfo = new ModelInfo(1L, "primary", 11L, 1);
        StreamingChatModel primary = this.prepareStreamingModel(mock(StreamingChatModel.class));
        ChatResponse successResponse = mock(ChatResponse.class);
        doAnswer(invocation -> {
            StreamingChatResponseHandler responseHandler = invocation.getArgument(1);
            responseHandler.onCompleteResponse(successResponse);
            return null;
        }).when(primary).doChat(any(ChatRequest.class), any(StreamingChatResponseHandler.class));
        FallbackModelTarget<StreamingChatModel> primaryTarget = this.createStreamingTarget(primaryInfo, primary);
        FallbackStreamingChatModel model = new FallbackStreamingChatModel(
                List.of(primaryTarget), List.of(), circuitBreaker, rateLimiter, usageRecorder);
        StreamingChatResponseHandler handler = mock(StreamingChatResponseHandler.class);
        ChatRequest request = this.createRequest();
        model.chat(request, handler);
        model.chat(request, handler);
        verify(handler, times(1)).onCompleteResponse(successResponse);
        ArgumentCaptor<Throwable> errorCaptor = ArgumentCaptor.forClass(Throwable.class);
        verify(handler, times(1)).onError(errorCaptor.capture());
        ModelInvocationException exception = (ModelInvocationException) errorCaptor.getValue();
        assertEquals(ModelErrorTypeEnum.RATE_LIMIT, exception.getErrorType());
        assertEquals(429, exception.getHttpStatus());
        verify(primary, times(1)).doChat(any(ChatRequest.class), any(StreamingChatResponseHandler.class));
        verify(usageRecorder).recordRateLimited(eq(primaryInfo), any(UsageSnapshot.class));
    }

    /**
     * 验证并发限额为1时两次顺序流式聊天均完成，流式终态释放并发许可不发生误拒
     */
    @Test
    void shouldAllowSequentialStreamingWhenConcurrentOne() {
        this.stubLimits("1", "0", "0");
        ModelInfo primaryInfo = new ModelInfo(1L, "primary", 11L, 1);
        StreamingChatModel primary = this.prepareStreamingModel(mock(StreamingChatModel.class));
        ChatResponse successResponse = mock(ChatResponse.class);
        doAnswer(invocation -> {
            StreamingChatResponseHandler responseHandler = invocation.getArgument(1);
            responseHandler.onCompleteResponse(successResponse);
            return null;
        }).when(primary).doChat(any(ChatRequest.class), any(StreamingChatResponseHandler.class));
        FallbackModelTarget<StreamingChatModel> primaryTarget = this.createStreamingTarget(primaryInfo, primary);
        FallbackStreamingChatModel model = new FallbackStreamingChatModel(
                List.of(primaryTarget), List.of(), circuitBreaker, rateLimiter, usageRecorder);
        StreamingChatResponseHandler handler = mock(StreamingChatResponseHandler.class);
        ChatRequest request = this.createRequest();
        model.chat(request, handler);
        model.chat(request, handler);
        verify(handler, times(2)).onCompleteResponse(successResponse);
        verify(handler, never()).onError(any());
    }

    /**
     * 验证同步聊天成功后回填响应token计数，TPM=100时一次消耗100token即触发下一次入口拒绝
     */
    @Test
    void shouldRecordTokensAndTriggerTpmReject() {
        this.stubLimits("0", "0", "100");
        ModelInfo primaryInfo = new ModelInfo(1L, "primary", 11L, 1);
        ChatModel primary = this.prepareChatModel(mock(ChatModel.class));
        ChatResponse successResponse = mock(ChatResponse.class);
        TokenUsage tokenUsage = new TokenUsage(40, 60);
        when(successResponse.tokenUsage()).thenReturn(tokenUsage);
        when(primary.doChat(any(ChatRequest.class))).thenReturn(successResponse);
        FallbackModelTarget<ChatModel> primaryTarget = this.createChatTarget(primaryInfo, primary);
        FallbackChatModel model = new FallbackChatModel(List.of(primaryTarget), List.of(), circuitBreaker, rateLimiter, usageRecorder);
        ChatRequest request = this.createRequest();
        ChatResponse response = model.chat(request);
        assertSame(successResponse, response);
        ModelInvocationException exception = assertThrows(ModelInvocationException.class, () -> model.chat(request));
        assertEquals(ModelErrorTypeEnum.RATE_LIMIT, exception.getErrorType());
        verify(primary, times(1)).doChat(any(ChatRequest.class));
    }

    /**
     * 验证流式完成回调同样回填token计数，TPM=100时一次完成消耗100token即触发下一次流式入口拒绝
     */
    @Test
    void shouldRecordStreamingTokensAndTriggerTpmReject() {
        this.stubLimits("0", "0", "100");
        ModelInfo primaryInfo = new ModelInfo(1L, "primary", 11L, 1);
        StreamingChatModel primary = this.prepareStreamingModel(mock(StreamingChatModel.class));
        ChatResponse successResponse = mock(ChatResponse.class);
        TokenUsage tokenUsage = new TokenUsage(40, 60);
        when(successResponse.tokenUsage()).thenReturn(tokenUsage);
        doAnswer(invocation -> {
            StreamingChatResponseHandler responseHandler = invocation.getArgument(1);
            responseHandler.onCompleteResponse(successResponse);
            return null;
        }).when(primary).doChat(any(ChatRequest.class), any(StreamingChatResponseHandler.class));
        FallbackModelTarget<StreamingChatModel> primaryTarget = this.createStreamingTarget(primaryInfo, primary);
        FallbackStreamingChatModel model = new FallbackStreamingChatModel(
                List.of(primaryTarget), List.of(), circuitBreaker, rateLimiter, usageRecorder);
        StreamingChatResponseHandler handler = mock(StreamingChatResponseHandler.class);
        ChatRequest request = this.createRequest();
        model.chat(request, handler);
        model.chat(request, handler);
        verify(handler, times(1)).onCompleteResponse(successResponse);
        ArgumentCaptor<Throwable> errorCaptor = ArgumentCaptor.forClass(Throwable.class);
        verify(handler, times(1)).onError(errorCaptor.capture());
        ModelInvocationException exception = (ModelInvocationException) errorCaptor.getValue();
        assertEquals(ModelErrorTypeEnum.RATE_LIMIT, exception.getErrorType());
        verify(primary, times(1)).doChat(any(ChatRequest.class), any(StreamingChatResponseHandler.class));
    }

    /**
     * 验证并发限额为1时流式终态错误也释放许可，第二次流式仍真实调用底层模型而非被限流误拒
     */
    @Test
    void shouldReleaseWhenStreamingErrorTerminal() {
        this.stubLimits("1", "0", "0");
        ModelInfo primaryInfo = new ModelInfo(1L, "primary", 11L, 1);
        StreamingChatModel primary = this.prepareStreamingModel(mock(StreamingChatModel.class));
        doAnswer(invocation -> {
            StreamingChatResponseHandler responseHandler = invocation.getArgument(1);
            responseHandler.onError(new HttpException(500, "primary error"));
            return null;
        }).when(primary).doChat(any(ChatRequest.class), any(StreamingChatResponseHandler.class));
        FallbackModelTarget<StreamingChatModel> primaryTarget = this.createStreamingTarget(primaryInfo, primary);
        FallbackStreamingChatModel model = new FallbackStreamingChatModel(
                List.of(primaryTarget), List.of(), circuitBreaker, rateLimiter, usageRecorder);
        StreamingChatResponseHandler handler = mock(StreamingChatResponseHandler.class);
        ChatRequest request = this.createRequest();
        model.chat(request, handler);
        model.chat(request, handler);
        ArgumentCaptor<Throwable> errorCaptor = ArgumentCaptor.forClass(Throwable.class);
        verify(handler, times(2)).onError(errorCaptor.capture());
        List<Throwable> errors = errorCaptor.getAllValues();
        for (Throwable error : errors) {
            ModelInvocationException exception = (ModelInvocationException) error;
            assertEquals(ModelErrorTypeEnum.SERVER, exception.getErrorType());
        }
        verify(primary, times(2)).doChat(any(ChatRequest.class), any(StreamingChatResponseHandler.class));
    }

    /**
     * 验证并发限额为1且全部目标熔断打开时流式立即收到503并释放许可，熔断重置后下一次流式正常完成
     */
    @Test
    void shouldReleaseWhenAllStreamingTargetsCircuitRejected() {
        this.stubLimits("1", "0", "0");
        ModelInfo primaryInfo = new ModelInfo(1L, "primary", 11L, 1);
        StreamingChatModel primary = this.prepareStreamingModel(mock(StreamingChatModel.class));
        ChatResponse successResponse = mock(ChatResponse.class);
        doAnswer(invocation -> {
            StreamingChatResponseHandler responseHandler = invocation.getArgument(1);
            responseHandler.onCompleteResponse(successResponse);
            return null;
        }).when(primary).doChat(any(ChatRequest.class), any(StreamingChatResponseHandler.class));
        for (int i = 0; i < 5; i++) {
            circuitBreaker.recordFailure(11L);
        }
        FallbackModelTarget<StreamingChatModel> primaryTarget = this.createStreamingTarget(primaryInfo, primary);
        FallbackStreamingChatModel model = new FallbackStreamingChatModel(
                List.of(primaryTarget), List.of(), circuitBreaker, rateLimiter, usageRecorder);
        StreamingChatResponseHandler handler = mock(StreamingChatResponseHandler.class);
        ChatRequest request = this.createRequest();
        model.chat(request, handler);
        ArgumentCaptor<Throwable> errorCaptor = ArgumentCaptor.forClass(Throwable.class);
        verify(handler, times(1)).onError(errorCaptor.capture());
        ModelInvocationException circuitException = (ModelInvocationException) errorCaptor.getValue();
        assertEquals(503, circuitException.getHttpStatus());
        circuitBreaker.reset(11L);
        model.chat(request, handler);
        verify(handler, times(1)).onCompleteResponse(successResponse);
        verify(primary, times(1)).doChat(any(ChatRequest.class), any(StreamingChatResponseHandler.class));
    }

    /**
     * 桩定当前租户三项限流配置
     */
    private void stubLimits(String maxConcurrent, String rpm, String tpm) {
        Map<String, String> configMap = new HashMap<>();
        configMap.put(TenantConfigEnum.LLM_MAX_CONCURRENT.getKey(), maxConcurrent);
        configMap.put(TenantConfigEnum.LLM_RPM.getKey(), rpm);
        configMap.put(TenantConfigEnum.LLM_TPM.getKey(), tpm);
        ResultData<Map<String, String>> configResult = new ResultData<>();
        configResult.setData(configMap);
        configResult.setCode(ResultData.OK);
        when(tenantConfigService.queryTenantConfigMap(103L)).thenReturn(configResult);
    }

    /**
     * 创建带独立重试的聊天目标
     */
    private FallbackModelTarget<ChatModel> createChatTarget(ModelInfo modelInfo, ChatModel delegate) {
        ChatModel invokingModel = modelInvoker.wrap(delegate, 0);
        return new FallbackModelTarget<>(modelInfo, invokingModel);
    }

    /**
     * 创建带独立重试的向量目标
     */
    private FallbackModelTarget<EmbeddingModel> createEmbeddingTarget(ModelInfo modelInfo, EmbeddingModel delegate) {
        EmbeddingModel invokingModel = modelInvoker.wrap(delegate, 0);
        return new FallbackModelTarget<>(modelInfo, invokingModel);
    }

    /**
     * 创建带独立重试的排序目标
     */
    private FallbackModelTarget<ScoringModel> createScoringTarget(ModelInfo modelInfo, ScoringModel delegate) {
        ScoringModel invokingModel = modelInvoker.wrap(delegate, 0);
        return new FallbackModelTarget<>(modelInfo, invokingModel);
    }

    /**
     * 创建带自身重试边界的流式目标
     */
    private FallbackModelTarget<StreamingChatModel> createStreamingTarget(ModelInfo modelInfo, StreamingChatModel delegate) {
        Long modelId = modelInfo.getModelId();
        StreamingChatModel invokingModel = modelInvoker.wrap(delegate, modelId, 60000L, 0);
        return new FallbackModelTarget<>(modelInfo, invokingModel);
    }

    /**
     * 配置聊天目标固定元数据和空监听器
     */
    private ChatModel prepareChatModel(ChatModel model) {
        when(model.defaultRequestParameters()).thenReturn(DefaultChatRequestParameters.EMPTY);
        when(model.listeners()).thenReturn(List.of());
        when(model.provider()).thenReturn(ModelProvider.OPEN_AI);
        Set<Capability> capabilities = Set.of();
        when(model.supportedCapabilities()).thenReturn(capabilities);
        return model;
    }

    /**
     * 配置流式目标固定元数据和空监听器
     */
    private StreamingChatModel prepareStreamingModel(StreamingChatModel model) {
        when(model.defaultRequestParameters()).thenReturn(DefaultChatRequestParameters.EMPTY);
        when(model.listeners()).thenReturn(List.of());
        when(model.provider()).thenReturn(ModelProvider.OPEN_AI);
        Set<Capability> capabilities = Set.of();
        when(model.supportedCapabilities()).thenReturn(capabilities);
        return model;
    }

    /**
     * 构造统一聊天请求
     */
    private ChatRequest createRequest() {
        return ChatRequest.builder().messages(UserMessage.from("question")).build();
    }
}