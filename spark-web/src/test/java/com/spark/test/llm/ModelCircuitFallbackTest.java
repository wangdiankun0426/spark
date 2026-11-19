package com.spark.test.llm;

import com.spark.common.bean.llm.dto.ModelInfo;
import com.spark.common.enums.CircuitStateEnum;
import com.spark.common.enums.ModelErrorTypeEnum;
import com.spark.llm.model.circuit.ProviderCircuitBreaker;
import com.spark.llm.model.error.ModelInvocationException;
import com.spark.llm.model.error.ModelRetryPolicy;
import com.spark.llm.model.fallback.FallbackChatModel;
import com.spark.llm.model.fallback.FallbackEmbeddingModel;
import com.spark.llm.model.fallback.FallbackModelTarget;
import com.spark.llm.model.fallback.FallbackScoringModel;
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
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.request.DefaultChatRequestParameters;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.model.scoring.ScoringModel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongSupplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
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
 * @since 2026-11-19 14:30:00
 * 同步降级链熔断接入测试
 */
public class ModelCircuitFallbackTest {
    private ModelInvoker modelInvoker;
    private ProviderCircuitBreaker circuitBreaker;
    private TenantRateLimiter rateLimiter;
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
        nowMillis = new AtomicLong(0L);
        LongSupplier clock = nowMillis::get;
        ReflectionTestUtils.setField(circuitBreaker, "clock", clock);
        rateLimiter = new TenantRateLimiter();
        ITenantConfigService tenantConfigService = mock(ITenantConfigService.class);
        ReflectionTestUtils.setField(rateLimiter, "tenantConfigService", tenantConfigService);
        usageRecorder = mock(ModelUsageRecorder.class);
    }

    @AfterEach
    void tearDown() {
        modelInvoker.destroy();
    }

    /**
     * 验证主供应商熔断打开后聊天请求零等待跳过主模型直接切备用成功，主模型不发生任何调用
     */
    @Test
    void shouldSkipOpenPrimaryWithZeroDelegateCalls() {
        ModelInfo primaryInfo = new ModelInfo(1L, "primary", 11L, 1);
        ModelInfo fallbackInfo = new ModelInfo(2L, "fallback", 22L, 1);
        ChatModel primary = this.prepareChatModel(mock(ChatModel.class));
        ChatModel fallback = this.prepareChatModel(mock(ChatModel.class));
        ChatResponse fallbackResponse = mock(ChatResponse.class);
        when(fallback.doChat(any(ChatRequest.class))).thenReturn(fallbackResponse);
        for (int i = 0; i < 5; i++) {
            circuitBreaker.recordFailure(11L);
        }
        FallbackModelTarget<ChatModel> primaryTarget = this.createChatTarget(primaryInfo, primary);
        FallbackModelTarget<ChatModel> fallbackTarget = this.createChatTarget(fallbackInfo, fallback);
        FallbackChatModel model = new FallbackChatModel(List.of(primaryTarget, fallbackTarget), List.of(), circuitBreaker, rateLimiter, usageRecorder);
        ChatRequest request = this.createRequest();
        ChatResponse response = model.chat(request);
        assertSame(fallbackResponse, response);
        verify(primary, never()).doChat(any(ChatRequest.class));
        verify(fallback, times(1)).doChat(any(ChatRequest.class));
        assertEquals(CircuitStateEnum.OPEN, circuitBreaker.getState(11L));
        assertEquals(CircuitStateEnum.CLOSED, circuitBreaker.getState(22L));
    }

    /**
     * 验证降级链全部供应商熔断打开时立即抛出503熔断异常，任何底层模型都不被调用
     */
    @Test
    void shouldFailFastWhenAllTargetsCircuitRejected() {
        ModelInfo primaryInfo = new ModelInfo(1L, "primary", 11L, 1);
        ModelInfo fallbackInfo = new ModelInfo(2L, "fallback", 22L, 1);
        ChatModel primary = this.prepareChatModel(mock(ChatModel.class));
        ChatModel fallback = this.prepareChatModel(mock(ChatModel.class));
        for (int i = 0; i < 5; i++) {
            circuitBreaker.recordFailure(11L);
            circuitBreaker.recordFailure(22L);
        }
        FallbackModelTarget<ChatModel> primaryTarget = this.createChatTarget(primaryInfo, primary);
        FallbackModelTarget<ChatModel> fallbackTarget = this.createChatTarget(fallbackInfo, fallback);
        FallbackChatModel model = new FallbackChatModel(List.of(primaryTarget, fallbackTarget), List.of(), circuitBreaker, rateLimiter, usageRecorder);
        ChatRequest request = this.createRequest();
        ModelInvocationException exception = assertThrows(ModelInvocationException.class, () -> model.chat(request));
        assertEquals(ModelErrorTypeEnum.SERVER, exception.getErrorType());
        assertEquals(503, exception.getHttpStatus());
        assertEquals("provider circuit open", exception.getMessage());
        verify(primary, never()).doChat(any(ChatRequest.class));
        verify(fallback, never()).doChat(any(ChatRequest.class));
    }

    /**
     * 验证单目标连续5次可重试最终失败打开熔断，第6次请求零等待抛熔断异常且底层模型只被调用5次
     */
    @Test
    void shouldOpenCircuitAfterFiveRetryableFailures() {
        ModelInfo primaryInfo = new ModelInfo(1L, "primary", 11L, 1);
        ChatModel primary = this.prepareChatModel(mock(ChatModel.class));
        when(primary.doChat(any(ChatRequest.class))).thenThrow(new HttpException(500, "primary error"));
        FallbackModelTarget<ChatModel> primaryTarget = this.createChatTarget(primaryInfo, primary);
        FallbackChatModel model = new FallbackChatModel(List.of(primaryTarget), List.of(), circuitBreaker, rateLimiter, usageRecorder);
        ChatRequest request = this.createRequest();
        for (int i = 0; i < 5; i++) {
            ModelInvocationException failure = assertThrows(ModelInvocationException.class, () -> model.chat(request));
            assertEquals(ModelErrorTypeEnum.SERVER, failure.getErrorType());
        }
        assertEquals(CircuitStateEnum.OPEN, circuitBreaker.getState(11L));
        ModelInvocationException exception = assertThrows(ModelInvocationException.class, () -> model.chat(request));
        assertEquals(503, exception.getHttpStatus());
        assertEquals("provider circuit open", exception.getMessage());
        verify(primary, times(5)).doChat(any(ChatRequest.class));
    }

    /**
     * 验证AUTH错误不计入熔断失败计数，连续6次AUTH失败后熔断仍闭合且每次都真实调用底层模型
     */
    @Test
    void shouldNotOpenCircuitOnAuthFailures() {
        ModelInfo primaryInfo = new ModelInfo(1L, "primary", 11L, 1);
        ChatModel primary = this.prepareChatModel(mock(ChatModel.class));
        when(primary.doChat(any(ChatRequest.class))).thenThrow(new HttpException(401, "unauthorized"));
        FallbackModelTarget<ChatModel> primaryTarget = this.createChatTarget(primaryInfo, primary);
        FallbackChatModel model = new FallbackChatModel(List.of(primaryTarget), List.of(), circuitBreaker, rateLimiter, usageRecorder);
        ChatRequest request = this.createRequest();
        for (int i = 0; i < 6; i++) {
            ModelInvocationException failure = assertThrows(ModelInvocationException.class, () -> model.chat(request));
            assertEquals(ModelErrorTypeEnum.AUTH, failure.getErrorType());
        }
        assertEquals(CircuitStateEnum.CLOSED, circuitBreaker.getState(11L));
        verify(primary, times(6)).doChat(any(ChatRequest.class));
    }

    /**
     * 验证聊天成功清零连续失败计数，4次失败后一次成功再连续4次失败仍不触发熔断
     */
    @Test
    void shouldResetFailureCountOnChatSuccess() {
        ModelInfo primaryInfo = new ModelInfo(1L, "primary", 11L, 1);
        ChatModel primary = this.prepareChatModel(mock(ChatModel.class));
        when(primary.doChat(any(ChatRequest.class))).thenThrow(new HttpException(500, "primary error"));
        FallbackModelTarget<ChatModel> primaryTarget = this.createChatTarget(primaryInfo, primary);
        FallbackChatModel model = new FallbackChatModel(List.of(primaryTarget), List.of(), circuitBreaker, rateLimiter, usageRecorder);
        ChatRequest request = this.createRequest();
        for (int i = 0; i < 4; i++) {
            assertThrows(ModelInvocationException.class, () -> model.chat(request));
        }
        assertEquals(CircuitStateEnum.CLOSED, circuitBreaker.getState(11L));
        ChatResponse successResponse = mock(ChatResponse.class);
        doReturn(successResponse).when(primary).doChat(any(ChatRequest.class));
        ChatResponse response = model.chat(request);
        assertSame(successResponse, response);
        doThrow(new HttpException(500, "primary error")).when(primary).doChat(any(ChatRequest.class));
        for (int i = 0; i < 4; i++) {
            assertThrows(ModelInvocationException.class, () -> model.chat(request));
        }
        assertEquals(CircuitStateEnum.CLOSED, circuitBreaker.getState(11L));
    }

    /**
     * 验证向量主供应商熔断打开后零等待跳过主模型直接由备用模型完成向量化
     */
    @Test
    void shouldSkipOpenPrimaryForEmbedding() {
        ModelInfo primaryInfo = new ModelInfo(1L, "primary", 11L, 2);
        ModelInfo fallbackInfo = new ModelInfo(2L, "fallback", 22L, 2);
        EmbeddingModel primary = mock(EmbeddingModel.class);
        EmbeddingModel fallback = mock(EmbeddingModel.class);
        List<TextSegment> segments = List.of(TextSegment.from("text"));
        Response<List<Embedding>> fallbackResponse = new Response<>(List.of());
        when(fallback.embedAll(segments)).thenReturn(fallbackResponse);
        for (int i = 0; i < 5; i++) {
            circuitBreaker.recordFailure(11L);
        }
        FallbackModelTarget<EmbeddingModel> primaryTarget = this.createEmbeddingTarget(primaryInfo, primary);
        FallbackModelTarget<EmbeddingModel> fallbackTarget = this.createEmbeddingTarget(fallbackInfo, fallback);
        FallbackEmbeddingModel model = new FallbackEmbeddingModel(List.of(primaryTarget, fallbackTarget), circuitBreaker, rateLimiter, usageRecorder);
        Response<List<Embedding>> response = model.embedAll(segments);
        assertSame(fallbackResponse, response);
        verify(primary, never()).embedAll(segments);
        verify(fallback, times(1)).embedAll(segments);
    }

    /**
     * 验证排序唯一目标供应商熔断打开时立即抛出503熔断异常，底层排序模型不被调用
     */
    @Test
    void shouldFailFastForScoringWhenCircuitRejected() {
        ModelInfo primaryInfo = new ModelInfo(1L, "primary", 11L, 3);
        ScoringModel primary = mock(ScoringModel.class);
        List<TextSegment> segments = List.of(TextSegment.from("text"));
        for (int i = 0; i < 5; i++) {
            circuitBreaker.recordFailure(11L);
        }
        FallbackModelTarget<ScoringModel> primaryTarget = this.createScoringTarget(primaryInfo, primary);
        FallbackScoringModel model = new FallbackScoringModel(List.of(primaryTarget), circuitBreaker, rateLimiter, usageRecorder);
        ModelInvocationException exception = assertThrows(ModelInvocationException.class,
                () -> model.scoreAll(segments, "query"));
        assertEquals(ModelErrorTypeEnum.SERVER, exception.getErrorType());
        assertEquals(503, exception.getHttpStatus());
        verify(primary, never()).scoreAll(segments, "query");
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
     * 构造统一聊天请求
     */
    private ChatRequest createRequest() {
        return ChatRequest.builder().messages(UserMessage.from("question")).build();
    }
}
