package com.spark.llm.model;

import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.CompleteToolCall;
import dev.langchain4j.model.chat.response.PartialThinking;
import dev.langchain4j.model.chat.response.PartialToolCall;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-11-17 10:00:00
 * 流式回调空闲看门狗，串行化终态并屏蔽超时后的迟到事件
 */
public class IdleTimeoutStreamingResponseHandler implements StreamingChatResponseHandler {
    private static final Logger logger = LoggerFactory.getLogger(IdleTimeoutStreamingResponseHandler.class);
    private final StreamingChatResponseHandler delegate;
    private final ScheduledThreadPoolExecutor scheduler;
    private final Executor callbackExecutor;
    private final Long modelId;
    private final long idleTimeoutMs;
    private ScheduledFuture<?> timeoutFuture;
    private boolean terminated;
    private long timeoutGeneration;

    public IdleTimeoutStreamingResponseHandler(StreamingChatResponseHandler delegate,
                                               ScheduledThreadPoolExecutor scheduler,
                                               Executor callbackExecutor,
                                               Long modelId, long idleTimeoutMs) {
        this.delegate = delegate;
        this.scheduler = scheduler;
        this.callbackExecutor = callbackExecutor;
        this.modelId = modelId;
        this.idleTimeoutMs = idleTimeoutMs;
    }

    /**
     * 启动首次空闲计时
     */
    public synchronized void start() {
        if (terminated) {
            return;
        }
        this.scheduleTimeout();
    }

    @Override
    public synchronized void onPartialResponse(String partialResponse) {
        if (!this.markActive()) {
            return;
        }
        delegate.onPartialResponse(partialResponse);
    }

    @Override
    public synchronized void onPartialThinking(PartialThinking partialThinking) {
        if (!this.markActive()) {
            return;
        }
        delegate.onPartialThinking(partialThinking);
    }

    @Override
    public synchronized void onPartialToolCall(PartialToolCall partialToolCall) {
        if (!this.markActive()) {
            return;
        }
        delegate.onPartialToolCall(partialToolCall);
    }

    @Override
    public synchronized void onCompleteToolCall(CompleteToolCall completeToolCall) {
        if (!this.markActive()) {
            return;
        }
        delegate.onCompleteToolCall(completeToolCall);
    }

    @Override
    public synchronized void onCompleteResponse(ChatResponse completeResponse) {
        if (terminated) {
            return;
        }
        terminated = true;
        this.cancelTimeout();
        delegate.onCompleteResponse(completeResponse);
    }

    @Override
    public synchronized void onError(Throwable error) {
        if (terminated) {
            return;
        }
        terminated = true;
        this.cancelTimeout();
        ModelInvocationException exception = ModelErrorClassifier.wrap(error);
        delegate.onError(exception);
    }

    /**
     * 刷新本次请求的空闲计时
     * @return 是否仍处于活跃状态
     */
    private boolean markActive() {
        if (terminated) {
            return false;
        }
        this.scheduleTimeout();
        return true;
    }

    /**
     * 重排空闲超时任务
     */
    private void scheduleTimeout() {
        timeoutGeneration++;
        long scheduledGeneration = timeoutGeneration;
        this.cancelTimeout();
        timeoutFuture = scheduler.schedule(() -> this.onIdleTimeout(scheduledGeneration),
                idleTimeoutMs, TimeUnit.MILLISECONDS);
    }

    /**
     * 取消当前空闲任务
     */
    private void cancelTimeout() {
        if (timeoutFuture == null) {
            return;
        }
        timeoutFuture.cancel(false);
        timeoutFuture = null;
    }

    /**
     * 空闲到期后终止本次流
     */
    private void onIdleTimeout(long scheduledGeneration) {
        synchronized (this) {
            if (terminated || scheduledGeneration != timeoutGeneration) {
                return;
            }
            terminated = true;
            timeoutFuture = null;
        }
        logger.warn("model streaming idle timeout, modelId={}, timeoutMs={}", modelId, idleTimeoutMs);
        ModelTimeoutException exception = new ModelTimeoutException(modelId, idleTimeoutMs);
        try {
            callbackExecutor.execute(() -> delegate.onError(exception));
        } catch (RejectedExecutionException e) {
            logger.warn("model timeout callback rejected, modelId={}", modelId);
        }
    }
}
