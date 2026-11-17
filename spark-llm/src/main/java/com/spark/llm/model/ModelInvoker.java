package com.spark.llm.model;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.scoring.ScoringModel;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-11-17 10:00:00
 * 模型统一调用层，负责模型包装与流式空闲超时调度
 */
@Component
public class ModelInvoker {
    public static final int DEFAULT_TIMEOUT_MS = 60000;
    public static final int MIN_TIMEOUT_MS = 1000;
    public static final int MAX_TIMEOUT_MS = 600000;
    private final ScheduledThreadPoolExecutor scheduler;
    private final ExecutorService callbackExecutor;
    private final AtomicLong callbackThreadSequence = new AtomicLong();

    public ModelInvoker() {
        this.scheduler = new ScheduledThreadPoolExecutor(1, runnable -> {
            Thread thread = new Thread(runnable, "model-invoker-scheduler");
            thread.setDaemon(true);
            return thread;
        });
        this.scheduler.setRemoveOnCancelPolicy(true);
        this.callbackExecutor = Executors.newFixedThreadPool(4, runnable -> {
            long threadNumber = callbackThreadSequence.incrementAndGet();
            Thread thread = new Thread(runnable, "model-invoker-callback-" + threadNumber);
            thread.setDaemon(true);
            return thread;
        });
    }

    /**
     * 包装同步聊天模型
     * @param delegate 原模型
     * @return 统一调用模型
     */
    public ChatModel wrap(ChatModel delegate) {
        if (delegate == null) {
            throw new IllegalArgumentException("chatModel must not be null");
        }
        return new InvokingChatModel(delegate, this);
    }

    /**
     * 包装流式聊天模型
     * @param delegate 原模型
     * @param modelId 模型id
     * @param idleTimeoutMs 流式空闲超时毫秒
     * @return 统一调用模型
     */
    public StreamingChatModel wrap(StreamingChatModel delegate, Long modelId, long idleTimeoutMs) {
        if (delegate == null || modelId == null) {
            throw new IllegalArgumentException("streamingChatModel and modelId must not be null");
        }
        if (idleTimeoutMs < MIN_TIMEOUT_MS || idleTimeoutMs > MAX_TIMEOUT_MS) {
            throw new IllegalArgumentException("idleTimeoutMs is out of range");
        }
        return new InvokingStreamingChatModel(delegate, this, modelId, idleTimeoutMs);
    }

    /**
     * 包装向量模型
     * @param delegate 原模型
     * @return 统一调用模型
     */
    public EmbeddingModel wrap(EmbeddingModel delegate) {
        if (delegate == null) {
            throw new IllegalArgumentException("embeddingModel must not be null");
        }
        return new InvokingEmbeddingModel(delegate, this);
    }

    /**
     * 包装排序模型
     * @param delegate 原模型
     * @return 统一调用模型
     */
    public ScoringModel wrap(ScoringModel delegate) {
        if (delegate == null) {
            throw new IllegalArgumentException("scoringModel must not be null");
        }
        return new InvokingScoringModel(delegate, this);
    }

    /**
     * 执行同步模型调用
     * @param invocation 调用逻辑
     * @return 调用结果
     * @param <T> 结果类型
     */
    public <T> T invoke(Supplier<T> invocation) {
        if (invocation == null) {
            throw new IllegalArgumentException("invocation must not be null");
        }
        try {
            return invocation.get();
        } catch (RuntimeException e) {
            throw ModelErrorClassifier.wrap(e);
        }
    }

    /**
     * 执行带空闲超时的流式模型调用
     * @param delegate 原模型
     * @param request 聊天请求
     * @param handler 业务回调
     * @param modelId 模型id
     * @param idleTimeoutMs 流式空闲超时毫秒
     */
    public void invoke(StreamingChatModel delegate, ChatRequest request, StreamingChatResponseHandler handler,
                       Long modelId, long idleTimeoutMs) {
        if (delegate == null || request == null || handler == null || modelId == null) {
            throw new IllegalArgumentException("streaming invocation params must not be null");
        }
        IdleTimeoutStreamingResponseHandler timeoutHandler = new IdleTimeoutStreamingResponseHandler(
                handler, scheduler, callbackExecutor, modelId, idleTimeoutMs);
        timeoutHandler.start();
        try {
            delegate.doChat(request, timeoutHandler);
        } catch (RuntimeException e) {
            timeoutHandler.onError(e);
        }
    }

    /**
     * 解析有效超时时长
     * @param timeoutMs 配置毫秒
     * @return 有效毫秒
     */
    public long resolveTimeout(Integer timeoutMs) {
        if (timeoutMs == null) {
            return DEFAULT_TIMEOUT_MS;
        }
        if (timeoutMs < MIN_TIMEOUT_MS || timeoutMs > MAX_TIMEOUT_MS) {
            throw new IllegalArgumentException("timeoutMs is out of range");
        }
        return timeoutMs;
    }

    /**
     * 关闭共享调度器
     */
    @PreDestroy
    public void destroy() {
        scheduler.shutdownNow();
        callbackExecutor.shutdownNow();
    }
}
