package com.spark.test.llm;

import com.spark.common.bean.llm.dto.ModelInfo;
import com.spark.llm.model.invocation.ModelInvoker;
import com.spark.llm.model.error.ModelRetryPolicy;
import com.spark.llm.model.credential.ProviderKeyChatModel;
import com.spark.llm.model.credential.ProviderKeyEmbeddingModel;
import com.spark.llm.model.credential.ProviderKeyModel;
import com.spark.llm.model.credential.ProviderKeyRouter;
import com.spark.llm.model.credential.ProviderKeyScoringModel;
import com.spark.llm.model.credential.ProviderKeyStreamingChatModel;
import com.spark.llm.usage.ModelUsageChatListener;
import com.spark.llm.usage.ModelUsageRecorder;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.exception.HttpException;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.request.DefaultChatRequestParameters;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatRequestParameters;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.model.scoring.ScoringModel;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-11-18 10:20:00
 * 按物理尝试切换凭据的模型测试
 */
public class ProviderKeyModelTest {

    /**
     * 验证同步Chat公开入口第一次Key1返回429后切换到Key2，最终只记录一条成功用量且retryCount为1
     */
    @Test
    void shouldSwitchChatKeyOnRetry() {
        ModelInvoker invoker = this.createDirectInvoker();
        ProviderKeyRouter router = mock(ProviderKeyRouter.class);
        ChatModel first = mock(ChatModel.class);
        ChatModel second = mock(ChatModel.class);
        OpenAiChatRequestParameters parameters = OpenAiChatRequestParameters.builder().modelName("test-model").build();
        ChatRequest request = ChatRequest.builder().messages(UserMessage.from("hello")).build();
        ChatResponse response = mock(ChatResponse.class);
        HttpException error = new HttpException(429, "limited");
        when(first.doChat(any())).thenThrow(error);
        when(second.doChat(any())).thenReturn(response);
        ModelUsageRecorder recorder = mock(ModelUsageRecorder.class);
        ModelInfo modelInfo = new ModelInfo(1L, "test-model", 10L, 1);
        ChatModelListener usageListener = new ModelUsageChatListener(recorder, modelInfo);
        List<ChatModelListener> listeners = List.of(usageListener);
        AtomicInteger selection = new AtomicInteger();
        ProviderKeyChatModel providerKeyModel = new ProviderKeyChatModel(() -> {
            if (selection.getAndIncrement() == 0) {
                return new ProviderKeyModel<>(10L, 1L, first);
            }
            return new ProviderKeyModel<>(10L, 2L, second);
        }, router, parameters, listeners);
        ChatModel model = invoker.wrap(providerKeyModel, 1);
        ChatResponse actual = model.chat(request);
        assertSame(response, actual);
        verify(router).handleError(10L, 1L, error);
        ArgumentCaptor<ChatRequest> firstRequestCaptor = ArgumentCaptor.forClass(ChatRequest.class);
        ArgumentCaptor<ChatRequest> secondRequestCaptor = ArgumentCaptor.forClass(ChatRequest.class);
        verify(first).doChat(firstRequestCaptor.capture());
        verify(second).doChat(secondRequestCaptor.capture());
        ChatRequest firstRequest = firstRequestCaptor.getValue();
        ChatRequest secondRequest = secondRequestCaptor.getValue();
        assertEquals("test-model", firstRequest.parameters().modelName());
        assertEquals("test-model", secondRequest.parameters().modelName());
        assertEquals(request.messages(), firstRequest.messages());
        assertEquals(request.messages(), secondRequest.messages());
        verify(recorder).record(eq(modelInfo), any(), isNull(), isNull(), isNull(), eq(1), eq(false));
        verifyNoMoreInteractions(recorder);
    }

    /**
     * 验证Embedding与Scoring在429后都重新选择Key2，当前Key1分别只冷却一次
     */
    @Test
    void shouldSwitchEmbeddingAndScoringKeysOnRetry() {
        ModelInvoker invoker = this.createDirectInvoker();
        ProviderKeyRouter router = mock(ProviderKeyRouter.class);
        HttpException error = new HttpException(429, "limited");
        List<TextSegment> segments = List.of();
        EmbeddingModel firstEmbedding = mock(EmbeddingModel.class);
        EmbeddingModel secondEmbedding = mock(EmbeddingModel.class);
        Response<List<Embedding>> embeddingResponse = new Response<>(List.of());
        when(firstEmbedding.embedAll(segments)).thenThrow(error);
        when(secondEmbedding.embedAll(segments)).thenReturn(embeddingResponse);
        AtomicInteger embeddingSelection = new AtomicInteger();
        ProviderKeyEmbeddingModel providerKeyEmbedding = new ProviderKeyEmbeddingModel(() -> {
            if (embeddingSelection.getAndIncrement() == 0) {
                return new ProviderKeyModel<>(10L, 1L, firstEmbedding);
            }
            return new ProviderKeyModel<>(10L, 2L, secondEmbedding);
        }, router, 1024);
        EmbeddingModel embeddingModel = invoker.wrap(providerKeyEmbedding, 1);
        assertSame(embeddingResponse, embeddingModel.embedAll(segments));

        ScoringModel firstScoring = mock(ScoringModel.class);
        ScoringModel secondScoring = mock(ScoringModel.class);
        Response<List<Double>> scoringResponse = new Response<>(List.of());
        when(firstScoring.scoreAll(segments, "query")).thenThrow(error);
        when(secondScoring.scoreAll(segments, "query")).thenReturn(scoringResponse);
        AtomicInteger scoringSelection = new AtomicInteger();
        ProviderKeyScoringModel providerKeyScoring = new ProviderKeyScoringModel(() -> {
            if (scoringSelection.getAndIncrement() == 0) {
                return new ProviderKeyModel<>(10L, 3L, firstScoring);
            }
            return new ProviderKeyModel<>(10L, 4L, secondScoring);
        }, router);
        ScoringModel scoringModel = invoker.wrap(providerKeyScoring, 1);
        assertSame(scoringResponse, scoringModel.scoreAll(segments, "query"));
        verify(router).handleError(10L, 1L, error);
        verify(router).handleError(10L, 3L, error);
    }

    /**
     * 验证流式公开入口首输出前Key1返回429后切换Key2，旧分片隔离且成功用量仅一条retryCount为1
     */
    @Test
    void shouldSwitchStreamingKeyBeforeFirstOutput() {
        ScheduledThreadPoolExecutor scheduler = mock(ScheduledThreadPoolExecutor.class);
        ModelInvoker invoker = this.createDirectInvoker(scheduler);
        ProviderKeyRouter router = mock(ProviderKeyRouter.class);
        StreamingChatModel first = mock(StreamingChatModel.class);
        StreamingChatModel second = mock(StreamingChatModel.class);
        OpenAiChatRequestParameters parameters = OpenAiChatRequestParameters.builder().modelName("test-model").build();
        ModelUsageRecorder recorder = mock(ModelUsageRecorder.class);
        ModelInfo modelInfo = new ModelInfo(1L, "test-model", 10L, 1);
        ChatModelListener usageListener = new ModelUsageChatListener(recorder, modelInfo);
        List<ChatModelListener> listeners = List.of(usageListener);
        AtomicInteger selection = new AtomicInteger();
        ProviderKeyStreamingChatModel providerKeyModel = new ProviderKeyStreamingChatModel(() -> {
            if (selection.getAndIncrement() == 0) {
                return new ProviderKeyModel<>(10L, 1L, first);
            }
            return new ProviderKeyModel<>(10L, 2L, second);
        }, router, parameters, listeners);
        StreamingChatModel model = invoker.wrap(providerKeyModel, 1L, 60000L, 1);
        ChatRequest request = ChatRequest.builder().messages(UserMessage.from("hello")).build();
        StreamingChatResponseHandler handler = mock(StreamingChatResponseHandler.class);
        model.chat(request, handler);
        ArgumentCaptor<StreamingChatResponseHandler> firstHandlerCaptor = ArgumentCaptor.forClass(StreamingChatResponseHandler.class);
        ArgumentCaptor<ChatRequest> firstRequestCaptor = ArgumentCaptor.forClass(ChatRequest.class);
        verify(first).doChat(firstRequestCaptor.capture(), firstHandlerCaptor.capture());
        StreamingChatResponseHandler firstHandler = firstHandlerCaptor.getValue();
        HttpException error = new HttpException(429, "limited");
        firstHandler.onError(error);
        verify(router).handleError(10L, 1L, error);
        ArgumentCaptor<Runnable> retryTaskCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(scheduler).schedule(retryTaskCaptor.capture(), eq(0L), eq(TimeUnit.MILLISECONDS));
        retryTaskCaptor.getValue().run();
        ArgumentCaptor<StreamingChatResponseHandler> secondHandlerCaptor = ArgumentCaptor.forClass(StreamingChatResponseHandler.class);
        ArgumentCaptor<ChatRequest> secondRequestCaptor = ArgumentCaptor.forClass(ChatRequest.class);
        verify(second).doChat(secondRequestCaptor.capture(), secondHandlerCaptor.capture());
        StreamingChatResponseHandler secondHandler = secondHandlerCaptor.getValue();
        ChatRequest firstRequest = firstRequestCaptor.getValue();
        ChatRequest secondRequest = secondRequestCaptor.getValue();
        assertEquals("test-model", firstRequest.parameters().modelName());
        assertEquals("test-model", secondRequest.parameters().modelName());
        assertEquals(request.messages(), firstRequest.messages());
        assertEquals(request.messages(), secondRequest.messages());
        secondHandler.onPartialResponse("ok");
        ChatResponse response = mock(ChatResponse.class);
        secondHandler.onCompleteResponse(response);
        verify(handler).onPartialResponse("ok");
        verify(handler).onCompleteResponse(response);
        verify(recorder).record(eq(modelInfo), any(), isNull(), isNull(), isNull(), eq(1), eq(false));
        verifyNoMoreInteractions(recorder);
        firstHandler.onPartialResponse("old");
        verify(handler, never()).onPartialResponse("old");
    }

    /**
     * 验证流式输出分片后Key1返回429只冷却当前Key，不调度重试且业务只收到一次最终错误
     */
    @Test
    void shouldCooldownWithoutRetryAfterFirstStreamingOutput() {
        ScheduledThreadPoolExecutor scheduler = mock(ScheduledThreadPoolExecutor.class);
        ModelInvoker invoker = this.createDirectInvoker(scheduler);
        ProviderKeyRouter router = mock(ProviderKeyRouter.class);
        StreamingChatModel first = mock(StreamingChatModel.class);
        ProviderKeyStreamingChatModel providerKeyModel = new ProviderKeyStreamingChatModel(
                () -> new ProviderKeyModel<>(10L, 1L, first), router, DefaultChatRequestParameters.EMPTY, List.of());
        StreamingChatModel model = invoker.wrap(providerKeyModel, 1L, 60000L, 3);
        ChatRequest request = mock(ChatRequest.class);
        StreamingChatResponseHandler handler = mock(StreamingChatResponseHandler.class);
        model.doChat(request, handler);
        ArgumentCaptor<StreamingChatResponseHandler> handlerCaptor = ArgumentCaptor.forClass(StreamingChatResponseHandler.class);
        verify(first).doChat(eq(request), handlerCaptor.capture());
        StreamingChatResponseHandler attemptHandler = handlerCaptor.getValue();
        attemptHandler.onPartialResponse("first");
        HttpException error = new HttpException(429, "limited");
        attemptHandler.onError(error);
        verify(handler).onPartialResponse("first");
        verify(router).handleError(10L, 1L, error);
        verify(handler).onError(any());
        verify(first, times(1)).doChat(eq(request), any());
        verify(scheduler, never()).schedule(any(Runnable.class), eq(0L), eq(TimeUnit.MILLISECONDS));
    }

    /**
     * 创建零等待且直接执行回调的调用器
     */
    private ModelInvoker createDirectInvoker() {
        ScheduledThreadPoolExecutor scheduler = mock(ScheduledThreadPoolExecutor.class);
        return this.createDirectInvoker(scheduler);
    }

    /**
     * 创建指定调度器的直接回调调用器
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
