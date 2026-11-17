package com.spark.test.llm;

import com.spark.common.enums.ModelErrorTypeEnum;
import com.spark.llm.model.IdleTimeoutStreamingResponseHandler;
import com.spark.llm.model.ModelInvocationException;
import com.spark.llm.model.ModelInvoker;
import com.spark.llm.model.ModelTimeoutException;
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
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
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

    @Test
    void shouldCancelTimeoutAfterComplete() throws InterruptedException {
        StreamingChatModel delegate = mock(StreamingChatModel.class);
        StreamingChatModel model = modelInvoker.wrap(delegate, 1L, 1000L);
        StreamingChatResponseHandler handler = mock(StreamingChatResponseHandler.class);
        ChatRequest request = mock(ChatRequest.class);
        model.doChat(request, handler);
        ArgumentCaptor<StreamingChatResponseHandler> handlerCaptor = ArgumentCaptor.forClass(StreamingChatResponseHandler.class);
        verify(delegate).doChat(eq(request), handlerCaptor.capture());
        StreamingChatResponseHandler timeoutHandler = handlerCaptor.getValue();
        ChatResponse response = mock(ChatResponse.class);
        timeoutHandler.onCompleteResponse(response);
        verify(handler).onCompleteResponse(response);
        Thread.sleep(1200);
        verify(handler, never()).onError(any());
    }

    @Test
    void shouldIgnoreLateEventsAfterTimeout() {
        StreamingChatModel delegate = mock(StreamingChatModel.class);
        StreamingChatModel model = modelInvoker.wrap(delegate, 1L, 1000L);
        StreamingChatResponseHandler handler = mock(StreamingChatResponseHandler.class);
        ChatRequest request = mock(ChatRequest.class);
        model.doChat(request, handler);
        ArgumentCaptor<StreamingChatResponseHandler> handlerCaptor = ArgumentCaptor.forClass(StreamingChatResponseHandler.class);
        verify(delegate).doChat(eq(request), handlerCaptor.capture());
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
        verify(delegate).doChat(eq(fastRequest), fastHandlerCaptor.capture());
        StreamingChatResponseHandler fastTimeoutHandler = fastHandlerCaptor.getValue();
        ChatResponse response = mock(ChatResponse.class);
        fastTimeoutHandler.onCompleteResponse(response);
        verify(fastHandler).onCompleteResponse(response);
        verify(slowHandler, timeout(2000)).onError(any(ModelTimeoutException.class));
        verify(fastHandler, never()).onError(any());
    }

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

    @Test
    void shouldClassifyStreamingError() {
        StreamingChatModel delegate = mock(StreamingChatModel.class);
        StreamingChatModel model = modelInvoker.wrap(delegate, 1L, 60000L);
        StreamingChatResponseHandler handler = mock(StreamingChatResponseHandler.class);
        ChatRequest request = mock(ChatRequest.class);
        model.doChat(request, handler);
        ArgumentCaptor<StreamingChatResponseHandler> handlerCaptor = ArgumentCaptor.forClass(StreamingChatResponseHandler.class);
        verify(delegate).doChat(eq(request), handlerCaptor.capture());
        StreamingChatResponseHandler timeoutHandler = handlerCaptor.getValue();
        HttpException error = new HttpException(429, "limited");
        timeoutHandler.onError(error);
        ArgumentCaptor<Throwable> errorCaptor = ArgumentCaptor.forClass(Throwable.class);
        verify(handler, times(1)).onError(errorCaptor.capture());
        ModelInvocationException exception = assertInstanceOf(ModelInvocationException.class, errorCaptor.getValue());
        assertEquals(ModelErrorTypeEnum.RATE_LIMIT, exception.getErrorType());
        assertEquals(429, exception.getHttpStatus());
        assertSame(error, exception.getCause());
    }

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
}
