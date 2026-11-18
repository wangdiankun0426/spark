package com.spark.test.llm;

import com.spark.common.bean.llm.dto.ModelInfo;
import com.spark.common.enums.ModelErrorTypeEnum;
import com.spark.llm.model.streaming.IdleTimeoutStreamingResponseHandler;
import com.spark.llm.model.error.ModelInvocationException;
import com.spark.llm.model.invocation.ModelInvoker;
import com.spark.llm.model.error.ModelRetryPolicy;
import com.spark.llm.model.error.ModelTimeoutException;
import com.spark.llm.usage.EmbeddingUsageWrapper;
import com.spark.llm.usage.ModelRetryContext;
import com.spark.llm.usage.ModelUsageRecorder;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.exception.HttpException;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.request.ChatRequestParameters;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.model.scoring.ScoringModel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
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
 * @since 2026-11-17 10:00:00
 * 模型统一调用层超时与委托测试
 */
public class ModelInvokerTest {
    private ModelInvoker modelInvoker;

    @BeforeEach
    void setUp() {
        modelInvoker = new ModelInvoker();
    }

    @AfterEach
    void tearDown() {
        modelInvoker.destroy();
    }

    /**
     * 验证流式持续无输出达到空闲时限后只推送一次超时异常且不触发完成回调
     */
    @Test
    void shouldTimeoutWhenStreamIdle() {
        StreamingChatModel delegate = mock(StreamingChatModel.class);
        StreamingChatModel model = modelInvoker.wrap(delegate, 1L, 1000L);
        StreamingChatResponseHandler handler = mock(StreamingChatResponseHandler.class);
        ChatRequest request = mock(ChatRequest.class);
        model.doChat(request, handler);
        ArgumentCaptor<Throwable> errorCaptor = ArgumentCaptor.forClass(Throwable.class);
        verify(handler, timeout(2000)).onError(errorCaptor.capture());
        ModelTimeoutException timeoutException = assertInstanceOf(ModelTimeoutException.class, errorCaptor.getValue());
        assertEquals(ModelErrorTypeEnum.TIMEOUT, timeoutException.getErrorType());
        assertTrue(timeoutException.getUserMessage().contains("1 秒"));
        verify(handler, never()).onCompleteResponse(any());
    }

    /**
     * 验证流式正常完成后空闲任务被取消，等待超过时限也不再推送异常
     */
    @Test
    void shouldCancelTimeoutAfterComplete() throws InterruptedException {
        StreamingChatModel delegate = mock(StreamingChatModel.class);
        StreamingChatModel model = modelInvoker.wrap(delegate, 1L, 1000L);
        StreamingChatResponseHandler handler = mock(StreamingChatResponseHandler.class);
        ChatRequest request = mock(ChatRequest.class);
        model.doChat(request, handler);
        ArgumentCaptor<StreamingChatResponseHandler> handlerCaptor = ArgumentCaptor.forClass(StreamingChatResponseHandler.class);
        verify(delegate, timeout(1000)).doChat(eq(request), handlerCaptor.capture());
        StreamingChatResponseHandler timeoutHandler = handlerCaptor.getValue();
        ChatResponse response = mock(ChatResponse.class);
        timeoutHandler.onCompleteResponse(response);
        verify(handler, timeout(1000)).onCompleteResponse(response);
        Thread.sleep(1200);
        verify(handler, never()).onError(any());
    }

    /**
     * 验证空闲超时后迟到的分片、完成和异常全部被忽略，业务终态不重复
     */
    @Test
    void shouldIgnoreLateEventsAfterTimeout() {
        StreamingChatModel delegate = mock(StreamingChatModel.class);
        StreamingChatModel model = modelInvoker.wrap(delegate, 1L, 1000L);
        StreamingChatResponseHandler handler = mock(StreamingChatResponseHandler.class);
        ChatRequest request = mock(ChatRequest.class);
        model.doChat(request, handler);
        ArgumentCaptor<StreamingChatResponseHandler> handlerCaptor = ArgumentCaptor.forClass(StreamingChatResponseHandler.class);
        verify(delegate, timeout(1000)).doChat(eq(request), handlerCaptor.capture());
        StreamingChatResponseHandler timeoutHandler = handlerCaptor.getValue();
        verify(handler, timeout(2000)).onError(any(ModelTimeoutException.class));
        timeoutHandler.onPartialResponse("late");
        ChatResponse lateResponse = mock(ChatResponse.class);
        timeoutHandler.onCompleteResponse(lateResponse);
        RuntimeException lateError = new RuntimeException("late error");
        timeoutHandler.onError(lateError);
        verify(handler, never()).onPartialResponse(anyString());
        verify(handler, never()).onCompleteResponse(any());
        verify(handler, times(1)).onError(any());
    }

    /**
     * 验证两个并发流共享模型时状态隔离，快速流正常完成而慢速流独立超时
     */
    @Test
    void shouldIsolateConcurrentStreamState() {
        StreamingChatModel delegate = mock(StreamingChatModel.class);
        StreamingChatModel model = modelInvoker.wrap(delegate, 1L, 1000L);
        StreamingChatResponseHandler slowHandler = mock(StreamingChatResponseHandler.class);
        StreamingChatResponseHandler fastHandler = mock(StreamingChatResponseHandler.class);
        ChatRequest slowRequest = mock(ChatRequest.class);
        ChatRequest fastRequest = mock(ChatRequest.class);
        model.doChat(slowRequest, slowHandler);
        model.doChat(fastRequest, fastHandler);
        ArgumentCaptor<StreamingChatResponseHandler> fastHandlerCaptor = ArgumentCaptor.forClass(StreamingChatResponseHandler.class);
        verify(delegate, timeout(1000)).doChat(eq(fastRequest), fastHandlerCaptor.capture());
        StreamingChatResponseHandler fastTimeoutHandler = fastHandlerCaptor.getValue();
        ChatResponse response = mock(ChatResponse.class);
        fastTimeoutHandler.onCompleteResponse(response);
        verify(fastHandler, timeout(1000)).onCompleteResponse(response);
        verify(slowHandler, timeout(2000)).onError(any(ModelTimeoutException.class));
        verify(fastHandler, never()).onError(any());
    }

    /**
     * 验证一个流的错误回调阻塞时不影响另一个流按时收到超时错误
     */
    @Test
    void shouldNotBlockOtherTimeoutsByCallback() throws InterruptedException {
        StreamingChatModel delegate = mock(StreamingChatModel.class);
        StreamingChatModel model = modelInvoker.wrap(delegate, 1L, 1000L);
        StreamingChatResponseHandler blockedHandler = mock(StreamingChatResponseHandler.class);
        StreamingChatResponseHandler normalHandler = mock(StreamingChatResponseHandler.class);
        CountDownLatch callbackStarted = new CountDownLatch(1);
        CountDownLatch releaseCallback = new CountDownLatch(1);
        doAnswer(invocation -> {
            callbackStarted.countDown();
            releaseCallback.await();
            return null;
        }).when(blockedHandler).onError(any());
        ChatRequest blockedRequest = mock(ChatRequest.class);
        ChatRequest normalRequest = mock(ChatRequest.class);
        try {
            model.doChat(blockedRequest, blockedHandler);
            model.doChat(normalRequest, normalHandler);
            assertTrue(callbackStarted.await(2, TimeUnit.SECONDS));
            verify(normalHandler, timeout(2000)).onError(any(ModelTimeoutException.class));
        } finally {
            releaseCallback.countDown();
        }
    }

    /**
     * 验证分片刷新计时后旧定时任务即使执行也不会误触发超时，仅当前任务有效
     */
    @Test
    void shouldIgnoreStaleTimeoutTask() {
        StreamingChatResponseHandler handler = mock(StreamingChatResponseHandler.class);
        ScheduledThreadPoolExecutor scheduler = mock(ScheduledThreadPoolExecutor.class);
        IdleTimeoutStreamingResponseHandler timeoutHandler = new IdleTimeoutStreamingResponseHandler(
                handler, scheduler, Runnable::run, 1L, 1000L);
        timeoutHandler.start();
        timeoutHandler.onPartialResponse("next");
        ArgumentCaptor<Runnable> taskCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(scheduler, times(2)).schedule(taskCaptor.capture(), eq(1000L), eq(TimeUnit.MILLISECONDS));
        List<Runnable> tasks = taskCaptor.getAllValues();
        Runnable staleTask = tasks.get(0);
        staleTask.run();
        verify(handler, never()).onError(any());
        Runnable currentTask = tasks.get(1);
        currentTask.run();
        verify(handler, times(1)).onError(any(ModelTimeoutException.class));
    }

    /**
     * 验证同步调用收到503时统一转换为SERVER模型异常并保留状态和原始异常
     */
    @Test
    void shouldClassifySyncInvocationError() {
        ChatModel chatDelegate = mock(ChatModel.class);
        ChatRequest request = mock(ChatRequest.class);
        HttpException error = new HttpException(503, "unavailable");
        when(chatDelegate.doChat(request)).thenThrow(error);
        ChatModel chatModel = modelInvoker.wrap(chatDelegate);
        ModelInvocationException exception = assertThrows(ModelInvocationException.class,
                () -> chatModel.doChat(request));
        assertEquals(ModelErrorTypeEnum.SERVER, exception.getErrorType());
        assertEquals(503, exception.getHttpStatus());
        assertSame(error, exception.getCause());
    }

    /**
     * 验证流式429经错误回调统一转换为RATE_LIMIT且只通知一次业务终态
     */
    @Test
    void shouldClassifyStreamingError() {
        StreamingChatModel delegate = mock(StreamingChatModel.class);
        StreamingChatModel model = modelInvoker.wrap(delegate, 1L, 60000L);
        StreamingChatResponseHandler handler = mock(StreamingChatResponseHandler.class);
        ChatRequest request = mock(ChatRequest.class);
        model.doChat(request, handler);
        ArgumentCaptor<StreamingChatResponseHandler> handlerCaptor = ArgumentCaptor.forClass(StreamingChatResponseHandler.class);
        verify(delegate, timeout(1000)).doChat(eq(request), handlerCaptor.capture());
        StreamingChatResponseHandler timeoutHandler = handlerCaptor.getValue();
        HttpException error = new HttpException(429, "limited");
        timeoutHandler.onError(error);
        ArgumentCaptor<Throwable> errorCaptor = ArgumentCaptor.forClass(Throwable.class);
        verify(handler, timeout(1000).times(1)).onError(errorCaptor.capture());
        ModelInvocationException exception = assertInstanceOf(ModelInvocationException.class, errorCaptor.getValue());
        assertEquals(ModelErrorTypeEnum.RATE_LIMIT, exception.getErrorType());
        assertEquals(429, exception.getHttpStatus());
        assertSame(error, exception.getCause());
    }

    /**
     * 验证同步503后实际重试一次并成功，第二次调用读取到retryCount为1且上下文已清理
     */
    @Test
    void shouldRetrySyncErrorAndSucceed() {
        ModelInvoker retryInvoker = this.createDirectInvoker();
        ChatModel delegate = mock(ChatModel.class);
        ChatRequest request = mock(ChatRequest.class);
        ChatResponse response = mock(ChatResponse.class);
        HttpException error = new HttpException(503, "unavailable");
        AtomicInteger successRetryCount = new AtomicInteger();
        when(delegate.doChat(request)).thenThrow(error).thenAnswer(invocation -> {
            ModelRetryContext context = ModelRetryContext.current();
            successRetryCount.set(context.getRetryCount());
            return response;
        });
        ChatModel model = retryInvoker.wrap(delegate, 2);
        assertSame(response, model.doChat(request));
        verify(delegate, times(2)).doChat(request);
        assertEquals(1, successRetryCount.get());
        assertNull(ModelRetryContext.current());
    }

    /**
     * 验证同步400属于不可重试错误，仅调用一次并抛出统一异常
     */
    @Test
    void shouldNotRetryInvalidSyncError() {
        ModelInvoker retryInvoker = this.createDirectInvoker();
        ChatModel delegate = mock(ChatModel.class);
        ChatRequest request = mock(ChatRequest.class);
        HttpException error = new HttpException(400, "bad request");
        when(delegate.doChat(request)).thenThrow(error);
        ChatModel model = retryInvoker.wrap(delegate, 3);
        assertThrows(ModelInvocationException.class, () -> model.doChat(request));
        verify(delegate, times(1)).doChat(request);
    }

    /**
     * 验证连续500达到重试上限后停止，总调用次数为配置值加一并保留SERVER分类
     */
    @Test
    void shouldStopAfterRetryExhaustion() {
        ModelInvoker retryInvoker = this.createDirectInvoker();
        ChatModel delegate = mock(ChatModel.class);
        ChatRequest request = mock(ChatRequest.class);
        HttpException error = new HttpException(500, "server error");
        when(delegate.doChat(request)).thenThrow(error);
        ChatModel model = retryInvoker.wrap(delegate, 2);
        ModelInvocationException exception = assertThrows(ModelInvocationException.class,
                () -> model.doChat(request));
        assertEquals(ModelErrorTypeEnum.SERVER, exception.getErrorType());
        verify(delegate, times(3)).doChat(request);
    }

    /**
     * 验证Embedding首次429后重试成功只记录一条用量且retryCount为1
     */
    @Test
    void shouldRecordEmbeddingRetryCountOnce() {
        ModelInvoker retryInvoker = this.createDirectInvoker();
        EmbeddingModel delegate = mock(EmbeddingModel.class);
        ModelUsageRecorder recorder = mock(ModelUsageRecorder.class);
        ModelInfo modelInfo = new ModelInfo(1L, "embedding", 2L, 2);
        EmbeddingUsageWrapper usageWrapper = new EmbeddingUsageWrapper(delegate, recorder, modelInfo);
        List<TextSegment> segments = List.of();
        Response<List<Embedding>> response = mock(Response.class);
        when(response.tokenUsage()).thenReturn(null);
        HttpException error = new HttpException(429, "limited");
        when(delegate.embedAll(segments)).thenThrow(error).thenReturn(response);
        EmbeddingModel model = retryInvoker.wrap(usageWrapper, 2);
        assertSame(response, model.embedAll(segments));
        verify(delegate, times(2)).embedAll(segments);
        verify(recorder, times(1)).record(eq(modelInfo), any(), isNull(), isNull(), isNull(), eq(1));
    }

    /**
     * 验证流式首输出前429可重试，第二次输出和完成仅下发一次且旧分片被隔离
     */
    @Test
    void shouldRetryStreamingErrorBeforeFirstOutput() {
        ScheduledThreadPoolExecutor scheduler = mock(ScheduledThreadPoolExecutor.class);
        ModelInvoker retryInvoker = this.createDirectInvoker(scheduler);
        StreamingChatModel delegate = mock(StreamingChatModel.class);
        StreamingChatModel model = retryInvoker.wrap(delegate, 1L, 60000L, 1);
        StreamingChatResponseHandler handler = mock(StreamingChatResponseHandler.class);
        ChatRequest request = mock(ChatRequest.class);
        model.doChat(request, handler);
        ArgumentCaptor<StreamingChatResponseHandler> firstAttemptCaptor = ArgumentCaptor.forClass(StreamingChatResponseHandler.class);
        verify(delegate).doChat(eq(request), firstAttemptCaptor.capture());
        StreamingChatResponseHandler firstAttempt = firstAttemptCaptor.getValue();
        firstAttempt.onError(new HttpException(429, "limited"));
        verify(handler, never()).onError(any());
        ArgumentCaptor<Runnable> retryTaskCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(scheduler).schedule(retryTaskCaptor.capture(), eq(0L), eq(TimeUnit.MILLISECONDS));
        Runnable retryTask = retryTaskCaptor.getValue();
        retryTask.run();
        ArgumentCaptor<StreamingChatResponseHandler> attemptsCaptor = ArgumentCaptor.forClass(StreamingChatResponseHandler.class);
        verify(delegate, times(2)).doChat(eq(request), attemptsCaptor.capture());
        StreamingChatResponseHandler secondAttempt = attemptsCaptor.getAllValues().get(1);
        secondAttempt.onPartialResponse("ok");
        verify(handler).onPartialResponse("ok");
        ChatResponse response = mock(ChatResponse.class);
        secondAttempt.onCompleteResponse(response);
        verify(handler).onCompleteResponse(response);
        firstAttempt.onPartialResponse("old");
        verify(handler, never()).onPartialResponse("old");
    }

    /**
     * 验证流式已输出分片后发生空闲超时不再重试，业务依次收到分片和最终错误
     */
    @Test
    void shouldNotRetryStreamingErrorAfterFirstOutput() {
        ModelInvoker retryInvoker = this.createDirectInvoker();
        StreamingChatModel delegate = mock(StreamingChatModel.class);
        StreamingChatModel model = retryInvoker.wrap(delegate, 1L, 60000L, 3);
        StreamingChatResponseHandler handler = mock(StreamingChatResponseHandler.class);
        ChatRequest request = mock(ChatRequest.class);
        model.doChat(request, handler);
        ArgumentCaptor<StreamingChatResponseHandler> attemptCaptor = ArgumentCaptor.forClass(StreamingChatResponseHandler.class);
        verify(delegate).doChat(eq(request), attemptCaptor.capture());
        StreamingChatResponseHandler attempt = attemptCaptor.getValue();
        attempt.onPartialResponse("first");
        attempt.onError(new ModelTimeoutException(1L, 60000L));
        verify(handler).onPartialResponse("first");
        verify(handler).onError(any(ModelTimeoutException.class));
        verify(delegate, times(1)).doChat(eq(request), any());
    }

    /**
     * 验证Chat、Embedding、Scoring同步包装只委托一次并保留响应和模型元数据
     */
    @Test
    void shouldDelegateSyncModelsOnce() {
        ChatModel chatDelegate = mock(ChatModel.class);
        ChatRequest request = mock(ChatRequest.class);
        ChatResponse chatResponse = mock(ChatResponse.class);
        ChatRequestParameters parameters = mock(ChatRequestParameters.class);
        when(chatDelegate.doChat(request)).thenReturn(chatResponse);
        when(chatDelegate.defaultRequestParameters()).thenReturn(parameters);
        ChatModel chatModel = modelInvoker.wrap(chatDelegate);
        assertSame(chatResponse, chatModel.doChat(request));
        assertSame(parameters, chatModel.defaultRequestParameters());
        verify(chatDelegate, times(1)).doChat(request);

        EmbeddingModel embeddingDelegate = mock(EmbeddingModel.class);
        List<TextSegment> segments = List.of();
        Response<List<Embedding>> embeddingResponse = new Response<>(List.of());
        when(embeddingDelegate.embedAll(segments)).thenReturn(embeddingResponse);
        EmbeddingModel embeddingModel = modelInvoker.wrap(embeddingDelegate);
        assertSame(embeddingResponse, embeddingModel.embedAll(segments));
        verify(embeddingDelegate, times(1)).embedAll(segments);

        ScoringModel scoringDelegate = mock(ScoringModel.class);
        Response<List<Double>> scoringResponse = new Response<>(List.of());
        when(scoringDelegate.scoreAll(segments, "query")).thenReturn(scoringResponse);
        ScoringModel scoringModel = modelInvoker.wrap(scoringDelegate);
        assertSame(scoringResponse, scoringModel.scoreAll(segments, "query"));
        verify(scoringDelegate, times(1)).scoreAll(segments, "query");
    }

    /**
     * 创建零等待且直接执行回调的调用器
     * @return 调用器
     */
    private ModelInvoker createDirectInvoker() {
        ScheduledThreadPoolExecutor scheduler = mock(ScheduledThreadPoolExecutor.class);
        return this.createDirectInvoker(scheduler);
    }

    /**
     * 创建指定调度器的直接回调调用器
     * @param scheduler 调度器
     * @return 调用器
     */
    private ModelInvoker createDirectInvoker(ScheduledThreadPoolExecutor scheduler) {
        ExecutorService callbackExecutor = mock(ExecutorService.class);
        doAnswer(invocation -> {
            Runnable task = invocation.getArgument(0);
            task.run();
            return null;
        }).when(callbackExecutor).execute(any(Runnable.class));
        ModelRetryPolicy retryPolicy = new ModelRetryPolicy(() -> 0);
        return new ModelInvoker(scheduler, callbackExecutor, retryPolicy);
    }
}
