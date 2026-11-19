package com.spark.test.llm;

import com.spark.common.bean.llm.dto.ModelInfo;
import com.spark.common.enums.ModelErrorTypeEnum;
import com.spark.llm.model.error.ModelInvocationException;
import com.spark.llm.model.fallback.FallbackChatModel;
import com.spark.llm.model.fallback.FallbackEmbeddingModel;
import com.spark.llm.model.fallback.FallbackModelTarget;
import com.spark.llm.model.fallback.FallbackScoringModel;
import com.spark.llm.model.invocation.ModelInvoker;
import com.spark.llm.model.error.ModelRetryPolicy;
import com.spark.llm.usage.EmbeddingUsageWrapper;
import com.spark.llm.usage.ModelRetryContext;
import com.spark.llm.usage.ModelUsageChatListener;
import com.spark.llm.usage.ModelUsageRecorder;
import com.spark.llm.usage.ScoringUsageWrapper;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.exception.HttpException;
import dev.langchain4j.model.ModelProvider;
import dev.langchain4j.model.chat.Capability;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.request.DefaultChatRequestParameters;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatRequestParameters;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.model.scoring.ScoringModel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
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
 * @since 2026-11-19 09:30:00
 * 同步模型跨供应商降级测试
 */
public class ModelFallbackTest {
    private ModelInvoker modelInvoker;
    private ModelUsageRecorder recorder;

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
        recorder = mock(ModelUsageRecorder.class);
    }

    @AfterEach
    void tearDown() {
        modelInvoker.destroy();
    }

    /**
     * 验证主聊天模型500重试一次仍失败后切备用成功，只记录一条备用模型用量且retryCount为1并标记降级
     */
    @Test
    void shouldFallbackChatAfterRetryExhaustion() {
        ModelInfo primaryInfo = new ModelInfo(1L, "primary", 11L, 1);
        ModelInfo fallbackInfo = new ModelInfo(2L, "fallback", 22L, 1);
        ChatModel primary = this.prepareChatModel(mock(ChatModel.class));
        ChatModel fallback = this.prepareChatModel(mock(ChatModel.class));
        ChatResponse fallbackResponse = mock(ChatResponse.class);
        when(primary.doChat(any(ChatRequest.class))).thenThrow(new HttpException(500, "primary error"));
        when(fallback.doChat(any(ChatRequest.class))).thenReturn(fallbackResponse);
        FallbackModelTarget<ChatModel> primaryTarget = this.createChatTarget(primaryInfo, primary, 1);
        FallbackModelTarget<ChatModel> fallbackTarget = this.createChatTarget(fallbackInfo, fallback, 0);
        ChatModelListener listener = new ModelUsageChatListener(recorder, primaryInfo);
        FallbackChatModel model = new FallbackChatModel(List.of(primaryTarget, fallbackTarget), List.of(listener));
        ChatRequest request = this.createRequest();
        ChatResponse response = model.chat(request);
        assertSame(fallbackResponse, response);
        verify(primary, times(2)).doChat(any(ChatRequest.class));
        verify(fallback, times(1)).doChat(any(ChatRequest.class));
        verify(recorder).record(eq(fallbackInfo), any(), isNull(), isNull(), isNull(), eq(1), eq(true));
        assertNull(ModelRetryContext.current());
    }

    /**
     * 验证主聊天模型直接成功时即使配置了备用模型也记录主模型且fallbackFlag为0
     */
    @Test
    void shouldNotMarkFallbackWhenPrimarySucceeds() {
        ModelInfo primaryInfo = new ModelInfo(1L, "primary", 11L, 1);
        ModelInfo fallbackInfo = new ModelInfo(2L, "fallback", 22L, 1);
        ChatModel primary = this.prepareChatModel(mock(ChatModel.class));
        ChatModel fallback = this.prepareChatModel(mock(ChatModel.class));
        ChatResponse primaryResponse = mock(ChatResponse.class);
        when(primary.doChat(any(ChatRequest.class))).thenReturn(primaryResponse);
        FallbackModelTarget<ChatModel> primaryTarget = this.createChatTarget(primaryInfo, primary, 0);
        FallbackModelTarget<ChatModel> fallbackTarget = this.createChatTarget(fallbackInfo, fallback, 0);
        ChatModelListener listener = new ModelUsageChatListener(recorder, primaryInfo);
        FallbackChatModel model = new FallbackChatModel(List.of(primaryTarget, fallbackTarget), List.of(listener));
        ChatRequest request = this.createRequest();
        ChatResponse response = model.chat(request);
        assertSame(primaryResponse, response);
        verify(fallback, never()).doChat(any(ChatRequest.class));
        verify(recorder).record(eq(primaryInfo), any(), isNull(), isNull(), isNull(), eq(0), eq(false));
    }

    /**
     * 验证主模型AUTH不执行自身重试并立即切备用，备用成功后只调用一次备用模型
     */
    @Test
    void shouldFallbackAuthWithoutRetryingPrimary() {
        ModelInfo primaryInfo = new ModelInfo(1L, "primary", 11L, 1);
        ModelInfo fallbackInfo = new ModelInfo(2L, "fallback", 22L, 1);
        ChatModel primary = this.prepareChatModel(mock(ChatModel.class));
        ChatModel fallback = this.prepareChatModel(mock(ChatModel.class));
        ChatResponse fallbackResponse = mock(ChatResponse.class);
        when(primary.doChat(any(ChatRequest.class))).thenThrow(new HttpException(401, "unauthorized"));
        when(fallback.doChat(any(ChatRequest.class))).thenReturn(fallbackResponse);
        FallbackModelTarget<ChatModel> primaryTarget = this.createChatTarget(primaryInfo, primary, 3);
        FallbackModelTarget<ChatModel> fallbackTarget = this.createChatTarget(fallbackInfo, fallback, 0);
        FallbackChatModel model = new FallbackChatModel(List.of(primaryTarget, fallbackTarget), List.of());
        ChatRequest request = this.createRequest();
        ChatResponse response = model.chat(request);
        assertSame(fallbackResponse, response);
        verify(primary, times(1)).doChat(any(ChatRequest.class));
        verify(fallback, times(1)).doChat(any(ChatRequest.class));
    }

    /**
     * 验证主模型INVALID请求错误直接失败，不尝试备用模型也不产生成功用量
     */
    @Test
    void shouldNotFallbackInvalidError() {
        ModelInfo primaryInfo = new ModelInfo(1L, "primary", 11L, 1);
        ModelInfo fallbackInfo = new ModelInfo(2L, "fallback", 22L, 1);
        ChatModel primary = this.prepareChatModel(mock(ChatModel.class));
        ChatModel fallback = this.prepareChatModel(mock(ChatModel.class));
        when(primary.doChat(any(ChatRequest.class))).thenThrow(new HttpException(400, "bad request"));
        FallbackModelTarget<ChatModel> primaryTarget = this.createChatTarget(primaryInfo, primary, 3);
        FallbackModelTarget<ChatModel> fallbackTarget = this.createChatTarget(fallbackInfo, fallback, 0);
        FallbackChatModel model = new FallbackChatModel(List.of(primaryTarget, fallbackTarget), List.of());
        ChatRequest request = this.createRequest();
        ModelInvocationException exception = assertThrows(ModelInvocationException.class, () -> model.chat(request));
        assertEquals(ModelErrorTypeEnum.INVALID, exception.getErrorType());
        verify(primary, times(1)).doChat(any(ChatRequest.class));
        verify(fallback, never()).doChat(any(ChatRequest.class));
    }

    /**
     * 验证主模型AUTH后备用模型合并自己的模型名温度与自定义参数，主模型思考参数不会泄漏到备用请求
     */
    @Test
    void shouldMergeFallbackRequestParametersIndependently() {
        ModelInfo primaryInfo = new ModelInfo(1L, "primary", 11L, 1);
        ModelInfo fallbackInfo = new ModelInfo(2L, "fallback", 22L, 1);
        ChatModel primary = mock(ChatModel.class);
        ChatModel fallback = mock(ChatModel.class);
        OpenAiChatRequestParameters primaryParameters = OpenAiChatRequestParameters.builder()
                .modelName("primary")
                .temperature(0.1)
                .customParameters(Map.of("enable_thinking", true))
                .build();
        OpenAiChatRequestParameters fallbackParameters = OpenAiChatRequestParameters.builder()
                .modelName("fallback")
                .temperature(0.7)
                .customParameters(Map.of("response_style", "brief"))
                .build();
        when(primary.defaultRequestParameters()).thenReturn(primaryParameters);
        when(fallback.defaultRequestParameters()).thenReturn(fallbackParameters);
        when(primary.listeners()).thenReturn(List.of());
        when(fallback.listeners()).thenReturn(List.of());
        when(primary.doChat(any(ChatRequest.class))).thenThrow(new HttpException(401, "unauthorized"));
        ChatResponse fallbackResponse = mock(ChatResponse.class);
        when(fallback.doChat(any(ChatRequest.class))).thenReturn(fallbackResponse);
        FallbackModelTarget<ChatModel> primaryTarget = this.createChatTarget(primaryInfo, primary, 0);
        FallbackModelTarget<ChatModel> fallbackTarget = this.createChatTarget(fallbackInfo, fallback, 0);
        FallbackChatModel model = new FallbackChatModel(List.of(primaryTarget, fallbackTarget), List.of());
        ChatRequest request = this.createRequest();
        ChatResponse response = model.chat(request);
        assertSame(fallbackResponse, response);
        ArgumentCaptor<ChatRequest> requestCaptor = ArgumentCaptor.forClass(ChatRequest.class);
        verify(fallback).doChat(requestCaptor.capture());
        ChatRequest fallbackRequest = requestCaptor.getValue();
        assertEquals("fallback", fallbackRequest.parameters().modelName());
        assertEquals(0.7, fallbackRequest.parameters().temperature());
        OpenAiChatRequestParameters actualParameters = (OpenAiChatRequestParameters) fallbackRequest.parameters();
        assertEquals("brief", actualParameters.customParameters().get("response_style"));
        assertFalse(actualParameters.customParameters().containsKey("enable_thinking"));
    }

    /**
     * 验证主备用聊天模型都失败时抛出最后备用模型异常，不向业务返回中间错误或成功响应
     */
    @Test
    void shouldThrowLastErrorAfterFallbackChainExhaustion() {
        ModelInfo primaryInfo = new ModelInfo(1L, "primary", 11L, 1);
        ModelInfo fallbackInfo = new ModelInfo(2L, "fallback", 22L, 1);
        ChatModel primary = this.prepareChatModel(mock(ChatModel.class));
        ChatModel fallback = this.prepareChatModel(mock(ChatModel.class));
        when(primary.doChat(any(ChatRequest.class))).thenThrow(new HttpException(500, "primary error"));
        when(fallback.doChat(any(ChatRequest.class))).thenThrow(new HttpException(503, "fallback error"));
        FallbackModelTarget<ChatModel> primaryTarget = this.createChatTarget(primaryInfo, primary, 0);
        FallbackModelTarget<ChatModel> fallbackTarget = this.createChatTarget(fallbackInfo, fallback, 0);
        FallbackChatModel model = new FallbackChatModel(List.of(primaryTarget, fallbackTarget), List.of());
        ChatRequest request = this.createRequest();
        ModelInvocationException exception = assertThrows(ModelInvocationException.class, () -> model.chat(request));
        assertEquals(503, exception.getHttpStatus());
        assertEquals(ModelErrorTypeEnum.SERVER, exception.getErrorType());
    }

    /**
     * 验证向量主模型重试耗尽后备用成功，只记录备用模型一条用量并保留累计重试和降级标志
     */
    @Test
    void shouldFallbackEmbeddingAndRecordActualModel() {
        ModelInfo primaryInfo = new ModelInfo(1L, "primary", 11L, 2);
        ModelInfo fallbackInfo = new ModelInfo(2L, "fallback", 22L, 2);
        EmbeddingModel primary = mock(EmbeddingModel.class);
        EmbeddingModel fallback = mock(EmbeddingModel.class);
        List<TextSegment> segments = List.of(TextSegment.from("text"));
        Response<List<Embedding>> fallbackResponse = new Response<>(List.of());
        when(primary.embedAll(segments)).thenThrow(new HttpException(500, "primary error"));
        when(fallback.embedAll(segments)).thenReturn(fallbackResponse);
        FallbackModelTarget<EmbeddingModel> primaryTarget = this.createEmbeddingTarget(primaryInfo, primary, 1);
        FallbackModelTarget<EmbeddingModel> fallbackTarget = this.createEmbeddingTarget(fallbackInfo, fallback, 0);
        FallbackEmbeddingModel fallbackModel = new FallbackEmbeddingModel(List.of(primaryTarget, fallbackTarget));
        EmbeddingUsageWrapper model = new EmbeddingUsageWrapper(fallbackModel, recorder, primaryInfo);
        Response<List<Embedding>> response = model.embedAll(segments);
        assertSame(fallbackResponse, response);
        verify(primary, times(2)).embedAll(segments);
        verify(recorder).record(eq(fallbackInfo), any(), isNull(), isNull(), isNull(), eq(1), eq(true));
    }

    /**
     * 验证排序主模型AUTH后立即切备用成功，只记录备用模型一条用量且主模型不重试
     */
    @Test
    void shouldFallbackScoringAndRecordActualModel() {
        ModelInfo primaryInfo = new ModelInfo(1L, "primary", 11L, 3);
        ModelInfo fallbackInfo = new ModelInfo(2L, "fallback", 22L, 3);
        ScoringModel primary = mock(ScoringModel.class);
        ScoringModel fallback = mock(ScoringModel.class);
        List<TextSegment> segments = List.of(TextSegment.from("text"));
        Response<List<Double>> fallbackResponse = new Response<>(List.of(1.0));
        when(primary.scoreAll(segments, "query")).thenThrow(new HttpException(403, "forbidden"));
        when(fallback.scoreAll(segments, "query")).thenReturn(fallbackResponse);
        FallbackModelTarget<ScoringModel> primaryTarget = this.createScoringTarget(primaryInfo, primary, 3);
        FallbackModelTarget<ScoringModel> fallbackTarget = this.createScoringTarget(fallbackInfo, fallback, 0);
        FallbackScoringModel fallbackModel = new FallbackScoringModel(List.of(primaryTarget, fallbackTarget));
        ScoringUsageWrapper model = new ScoringUsageWrapper(fallbackModel, recorder, primaryInfo);
        Response<List<Double>> response = model.scoreAll(segments, "query");
        assertSame(fallbackResponse, response);
        verify(primary, times(1)).scoreAll(segments, "query");
        verify(recorder).record(eq(fallbackInfo), any(), isNull(), isNull(), isNull(), eq(0), eq(true));
    }

    /**
     * 创建带独立重试的聊天目标
     */
    private FallbackModelTarget<ChatModel> createChatTarget(ModelInfo modelInfo, ChatModel delegate, int maxRetries) {
        ChatModel invokingModel = modelInvoker.wrap(delegate, maxRetries);
        return new FallbackModelTarget<>(modelInfo, invokingModel);
    }

    /**
     * 创建带独立重试的向量目标
     */
    private FallbackModelTarget<EmbeddingModel> createEmbeddingTarget(
            ModelInfo modelInfo, EmbeddingModel delegate, int maxRetries) {
        EmbeddingModel invokingModel = modelInvoker.wrap(delegate, maxRetries);
        return new FallbackModelTarget<>(modelInfo, invokingModel);
    }

    /**
     * 创建带独立重试的排序目标
     */
    private FallbackModelTarget<ScoringModel> createScoringTarget(
            ModelInfo modelInfo, ScoringModel delegate, int maxRetries) {
        ScoringModel invokingModel = modelInvoker.wrap(delegate, maxRetries);
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
