package com.spark.test.llm;

import com.spark.common.bean.llm.dto.ModelInfo;
import com.spark.common.enums.CircuitStateEnum;
import com.spark.common.enums.ModelErrorTypeEnum;
import com.spark.llm.model.circuit.ProviderCircuitBreaker;
import com.spark.llm.model.error.ModelInvocationException;
import com.spark.llm.model.error.ModelRetryPolicy;
import com.spark.llm.model.fallback.FallbackModelTarget;
import com.spark.llm.model.fallback.FallbackStreamingChatModel;
import com.spark.llm.model.invocation.ModelInvoker;
import com.spark.llm.usage.ModelUsageChatListener;
import com.spark.llm.usage.ModelUsageRecorder;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.exception.HttpException;
import dev.langchain4j.model.ModelProvider;
import dev.langchain4j.model.chat.Capability;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.request.DefaultChatRequestParameters;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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
 * @since 2026-11-19 09:40:00
 * 流式聊天跨供应商降级测试
 */
public class FallbackStreamingChatModelTest {
    private ModelInvoker modelInvoker;
    private ModelUsageRecorder recorder;
    private ProviderCircuitBreaker circuitBreaker;

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
        circuitBreaker = new ProviderCircuitBreaker();
    }

    @AfterEach
    void tearDown() {
        modelInvoker.destroy();
    }

    /**
     * 验证主流式模型首输出前500后切备用成功，旧主模型迟到分片被隔离且只记录备用模型一条降级用量
     */
    @Test
    void shouldFallbackBeforeFirstOutputAndIgnoreStaleEvents() {
        ModelInfo primaryInfo = new ModelInfo(1L, "primary", 11L, 1);
        ModelInfo fallbackInfo = new ModelInfo(2L, "fallback", 22L, 1);
        StreamingChatModel primary = this.prepareStreamingModel(mock(StreamingChatModel.class));
        StreamingChatModel fallback = this.prepareStreamingModel(mock(StreamingChatModel.class));
        doAnswer(invocation -> {
            StreamingChatResponseHandler handler = invocation.getArgument(1);
            handler.onError(new HttpException(500, "primary error"));
            return null;
        }).when(primary).doChat(any(ChatRequest.class), any(StreamingChatResponseHandler.class));
        ChatResponse fallbackResponse = mock(ChatResponse.class);
        doAnswer(invocation -> {
            StreamingChatResponseHandler handler = invocation.getArgument(1);
            handler.onPartialResponse("ok");
            handler.onCompleteResponse(fallbackResponse);
            return null;
        }).when(fallback).doChat(any(ChatRequest.class), any(StreamingChatResponseHandler.class));
        FallbackModelTarget<StreamingChatModel> primaryTarget = this.createStreamingTarget(primaryInfo, primary);
        FallbackModelTarget<StreamingChatModel> fallbackTarget = this.createStreamingTarget(fallbackInfo, fallback);
        ChatModelListener listener = new ModelUsageChatListener(recorder, primaryInfo);
        FallbackStreamingChatModel model = new FallbackStreamingChatModel(
                List.of(primaryTarget, fallbackTarget), List.of(listener), circuitBreaker);
        StreamingChatResponseHandler handler = mock(StreamingChatResponseHandler.class);
        ChatRequest request = this.createRequest();
        model.chat(request, handler);
        verify(handler).onPartialResponse("ok");
        verify(handler).onCompleteResponse(fallbackResponse);
        verify(recorder).record(eq(fallbackInfo), any(), isNull(), isNull(), isNull(), eq(0), eq(true));
        ArgumentCaptor<StreamingChatResponseHandler> primaryHandlerCaptor = ArgumentCaptor.forClass(
                StreamingChatResponseHandler.class);
        verify(primary).doChat(any(ChatRequest.class), primaryHandlerCaptor.capture());
        StreamingChatResponseHandler staleHandler = primaryHandlerCaptor.getValue();
        staleHandler.onPartialResponse("old");
        verify(handler, never()).onPartialResponse("old");
    }

    /**
     * 验证备用流式模型已经输出分片后再发生500时不切第三模型，业务只收到原分片和一次最终错误
     */
    @Test
    void shouldNotFallbackAgainAfterFallbackOutputStarted() {
        ModelInfo primaryInfo = new ModelInfo(1L, "primary", 11L, 1);
        ModelInfo fallbackInfo = new ModelInfo(2L, "fallback", 22L, 1);
        ModelInfo thirdInfo = new ModelInfo(3L, "third", 33L, 1);
        StreamingChatModel primary = this.prepareStreamingModel(mock(StreamingChatModel.class));
        StreamingChatModel fallback = this.prepareStreamingModel(mock(StreamingChatModel.class));
        StreamingChatModel third = this.prepareStreamingModel(mock(StreamingChatModel.class));
        doAnswer(invocation -> {
            StreamingChatResponseHandler handler = invocation.getArgument(1);
            handler.onError(new HttpException(500, "primary error"));
            return null;
        }).when(primary).doChat(any(ChatRequest.class), any(StreamingChatResponseHandler.class));
        doAnswer(invocation -> {
            StreamingChatResponseHandler handler = invocation.getArgument(1);
            handler.onPartialResponse("partial");
            handler.onError(new HttpException(500, "fallback error"));
            return null;
        }).when(fallback).doChat(any(ChatRequest.class), any(StreamingChatResponseHandler.class));
        FallbackModelTarget<StreamingChatModel> primaryTarget = this.createStreamingTarget(primaryInfo, primary);
        FallbackModelTarget<StreamingChatModel> fallbackTarget = this.createStreamingTarget(fallbackInfo, fallback);
        FallbackModelTarget<StreamingChatModel> thirdTarget = this.createStreamingTarget(thirdInfo, third);
        FallbackStreamingChatModel model = new FallbackStreamingChatModel(
                List.of(primaryTarget, fallbackTarget, thirdTarget), List.of(), circuitBreaker);
        StreamingChatResponseHandler handler = mock(StreamingChatResponseHandler.class);
        ChatRequest request = this.createRequest();
        model.chat(request, handler);
        verify(handler).onPartialResponse("partial");
        verify(handler).onError(any());
        verify(handler, never()).onCompleteResponse(any());
        verify(third, never()).doChat(any(ChatRequest.class), any(StreamingChatResponseHandler.class));
    }

    /**
     * 验证主供应商熔断打开后流式请求零等待跳过主模型直接由备用模型完成输出，主模型不发生任何调用
     */
    @Test
    void shouldSkipCircuitOpenPrimaryAndStreamFallback() {
        ModelInfo primaryInfo = new ModelInfo(1L, "primary", 11L, 1);
        ModelInfo fallbackInfo = new ModelInfo(2L, "fallback", 22L, 1);
        StreamingChatModel primary = this.prepareStreamingModel(mock(StreamingChatModel.class));
        StreamingChatModel fallback = this.prepareStreamingModel(mock(StreamingChatModel.class));
        ChatResponse fallbackResponse = mock(ChatResponse.class);
        doAnswer(invocation -> {
            StreamingChatResponseHandler handler = invocation.getArgument(1);
            handler.onPartialResponse("ok");
            handler.onCompleteResponse(fallbackResponse);
            return null;
        }).when(fallback).doChat(any(ChatRequest.class), any(StreamingChatResponseHandler.class));
        for (int i = 0; i < 5; i++) {
            circuitBreaker.recordFailure(11L);
        }
        FallbackModelTarget<StreamingChatModel> primaryTarget = this.createStreamingTarget(primaryInfo, primary);
        FallbackModelTarget<StreamingChatModel> fallbackTarget = this.createStreamingTarget(fallbackInfo, fallback);
        FallbackStreamingChatModel model = new FallbackStreamingChatModel(
                List.of(primaryTarget, fallbackTarget), List.of(), circuitBreaker);
        StreamingChatResponseHandler handler = mock(StreamingChatResponseHandler.class);
        ChatRequest request = this.createRequest();
        model.chat(request, handler);
        verify(handler).onPartialResponse("ok");
        verify(handler).onCompleteResponse(fallbackResponse);
        verify(primary, never()).doChat(any(ChatRequest.class), any(StreamingChatResponseHandler.class));
        assertEquals(CircuitStateEnum.OPEN, circuitBreaker.getState(11L));
        assertEquals(CircuitStateEnum.CLOSED, circuitBreaker.getState(22L));
    }

    /**
     * 验证单目标流式连续5次可重试终态错误后熔断打开，第6次请求零等待收到503熔断异常且底层模型只被调用5次
     */
    @Test
    void shouldOpenCircuitAfterFiveStreamingFailures() {
        ModelInfo primaryInfo = new ModelInfo(1L, "primary", 11L, 1);
        StreamingChatModel primary = this.prepareStreamingModel(mock(StreamingChatModel.class));
        doAnswer(invocation -> {
            StreamingChatResponseHandler handler = invocation.getArgument(1);
            handler.onError(new HttpException(500, "primary error"));
            return null;
        }).when(primary).doChat(any(ChatRequest.class), any(StreamingChatResponseHandler.class));
        FallbackModelTarget<StreamingChatModel> primaryTarget = this.createStreamingTarget(primaryInfo, primary);
        FallbackStreamingChatModel model = new FallbackStreamingChatModel(List.of(primaryTarget), List.of(), circuitBreaker);
        StreamingChatResponseHandler handler = mock(StreamingChatResponseHandler.class);
        ChatRequest request = this.createRequest();
        for (int i = 0; i < 6; i++) {
            model.chat(request, handler);
        }
        assertEquals(CircuitStateEnum.OPEN, circuitBreaker.getState(11L));
        verify(primary, times(5)).doChat(any(ChatRequest.class), any(StreamingChatResponseHandler.class));
        ArgumentCaptor<Throwable> errorCaptor = ArgumentCaptor.forClass(Throwable.class);
        verify(handler, times(6)).onError(errorCaptor.capture());
        List<Throwable> errors = errorCaptor.getAllValues();
        Throwable lastError = errors.get(5);
        ModelInvocationException circuitException = (ModelInvocationException) lastError;
        assertEquals(ModelErrorTypeEnum.SERVER, circuitException.getErrorType());
        assertEquals(503, circuitException.getHttpStatus());
    }

    /**
     * 验证流式完成回调清零连续失败计数，4次失败后一次成功再连续4次失败仍不触发熔断
     */
    @Test
    void shouldResetFailureCountOnStreamingSuccess() {
        ModelInfo primaryInfo = new ModelInfo(1L, "primary", 11L, 1);
        StreamingChatModel primary = this.prepareStreamingModel(mock(StreamingChatModel.class));
        doAnswer(invocation -> {
            StreamingChatResponseHandler handler = invocation.getArgument(1);
            handler.onError(new HttpException(500, "primary error"));
            return null;
        }).when(primary).doChat(any(ChatRequest.class), any(StreamingChatResponseHandler.class));
        FallbackModelTarget<StreamingChatModel> primaryTarget = this.createStreamingTarget(primaryInfo, primary);
        FallbackStreamingChatModel model = new FallbackStreamingChatModel(List.of(primaryTarget), List.of(), circuitBreaker);
        StreamingChatResponseHandler handler = mock(StreamingChatResponseHandler.class);
        ChatRequest request = this.createRequest();
        for (int i = 0; i < 4; i++) {
            model.chat(request, handler);
        }
        assertEquals(CircuitStateEnum.CLOSED, circuitBreaker.getState(11L));
        ChatResponse successResponse = mock(ChatResponse.class);
        doAnswer(invocation -> {
            StreamingChatResponseHandler responseHandler = invocation.getArgument(1);
            responseHandler.onCompleteResponse(successResponse);
            return null;
        }).when(primary).doChat(any(ChatRequest.class), any(StreamingChatResponseHandler.class));
        model.chat(request, handler);
        doAnswer(invocation -> {
            StreamingChatResponseHandler responseHandler = invocation.getArgument(1);
            responseHandler.onError(new HttpException(500, "primary error"));
            return null;
        }).when(primary).doChat(any(ChatRequest.class), any(StreamingChatResponseHandler.class));
        for (int i = 0; i < 4; i++) {
            model.chat(request, handler);
        }
        assertEquals(CircuitStateEnum.CLOSED, circuitBreaker.getState(11L));
        verify(handler).onCompleteResponse(successResponse);
    }

    /**
     * 创建带自身重试边界的流式目标
     */
    private FallbackModelTarget<StreamingChatModel> createStreamingTarget(
            ModelInfo modelInfo, StreamingChatModel delegate) {
        Long modelId = modelInfo.getModelId();
        StreamingChatModel invokingModel = modelInvoker.wrap(delegate, modelId, 60000L, 0);
        return new FallbackModelTarget<>(modelInfo, invokingModel);
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
     * 构造统一流式聊天请求
     */
    private ChatRequest createRequest() {
        return ChatRequest.builder().messages(UserMessage.from("question")).build();
    }
}
