package com.spark.llm.model.streaming;

import com.spark.llm.model.error.ModelErrorClassifier;
import com.spark.llm.model.error.ModelInvocationException;
import com.spark.llm.model.error.ModelRetryPolicy;

import com.spark.llm.usage.ModelRetryContext;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.CompleteToolCall;
import dev.langchain4j.model.chat.response.PartialThinking;
import dev.langchain4j.model.chat.response.PartialToolCall;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executor;
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
 * @since 2026-11-18 09:00:00
 * 流式逻辑调用协调器，控制首输出前重试与唯一业务终态
 */
public class RetryingStreamingInvocation {
    private static final Logger logger = LoggerFactory.getLogger(RetryingStreamingInvocation.class);
    private static final int ACTIVE = 0;
    private static final int WAITING_RETRY = 1;
    private static final int TERMINAL = 2;
    private final StreamingChatModel delegate;
    private final ChatRequest request;
    private final StreamingChatResponseHandler handler;
    private final ScheduledThreadPoolExecutor scheduler;
    private final SerialTaskExecutor serialExecutor;
    private final Long modelId;
    private final long idleTimeoutMs;
    private final int maxRetries;
    private final ModelRetryPolicy retryPolicy;
    private final ModelRetryContext retryContext;
    private long activeAttempt;
    private int state = ACTIVE;
    private boolean outputStarted;
    private int retryCount;
    private ScheduledFuture<?> retryFuture;

    public RetryingStreamingInvocation(StreamingChatModel delegate, ChatRequest request,
                                       StreamingChatResponseHandler handler,
                                       ScheduledThreadPoolExecutor scheduler, Executor callbackExecutor,
                                       Long modelId, long idleTimeoutMs, int maxRetries,
                                       ModelRetryPolicy retryPolicy, ModelRetryContext retryContext) {
        if (delegate == null || request == null || handler == null || scheduler == null
                || callbackExecutor == null || modelId == null || retryPolicy == null || retryContext == null) {
            throw new IllegalArgumentException("streaming retry params must not be null");
        }
        this.delegate = delegate;
        this.request = request;
        this.handler = handler;
        this.scheduler = scheduler;
        this.serialExecutor = new SerialTaskExecutor(callbackExecutor);
        this.modelId = modelId;
        this.idleTimeoutMs = idleTimeoutMs;
        this.maxRetries = maxRetries;
        this.retryPolicy = retryPolicy;
        this.retryContext = retryContext;
    }

    /**
     * 启动首次流式调用
     */
    public void start() {
        serialExecutor.execute(() -> this.startAttempt(0));
    }

    /**
     * 接收回答分片
     * @param attemptId 尝试id
     * @param partialResponse 回答分片
     */
    public void onPartialResponse(long attemptId, String partialResponse) {
        serialExecutor.execute(() -> this.handlePartialResponse(attemptId, partialResponse));
    }

    /**
     * 接收思考分片
     * @param attemptId 尝试id
     * @param partialThinking 思考分片
     */
    public void onPartialThinking(long attemptId, PartialThinking partialThinking) {
        serialExecutor.execute(() -> this.handlePartialThinking(attemptId, partialThinking));
    }

    /**
     * 接收工具调用分片
     * @param attemptId 尝试id
     * @param partialToolCall 工具调用分片
     */
    public void onPartialToolCall(long attemptId, PartialToolCall partialToolCall) {
        serialExecutor.execute(() -> this.handlePartialToolCall(attemptId, partialToolCall));
    }

    /**
     * 接收完整工具调用
     * @param attemptId 尝试id
     * @param completeToolCall 完整工具调用
     */
    public void onCompleteToolCall(long attemptId, CompleteToolCall completeToolCall) {
        serialExecutor.execute(() -> this.handleCompleteToolCall(attemptId, completeToolCall));
    }

    /**
     * 接收单次尝试完成事件
     * @param attemptId 尝试id
     * @param completeResponse 完成响应
     */
    public void onCompleteResponse(long attemptId, ChatResponse completeResponse) {
        serialExecutor.execute(() -> this.handleCompleteResponse(attemptId, completeResponse));
    }

    /**
     * 接收单次尝试异常
     * @param attemptId 尝试id
     * @param error 异常
     */
    public void onError(long attemptId, Throwable error) {
        serialExecutor.execute(() -> this.handleError(attemptId, error));
    }

    /**
     * 启动指定尝试
     * @param attemptId 尝试id
     */
    private void startAttempt(long attemptId) {
        if (state == TERMINAL || activeAttempt != attemptId) {
            return;
        }
        if (attemptId > 0 && state != WAITING_RETRY) {
            return;
        }
        if (attemptId > 0) {
            retryCount = retryContext.incrementRetryCount();
        }
        state = ACTIVE;
        retryFuture = null;
        AttemptStreamingResponseHandler attemptHandler = new AttemptStreamingResponseHandler(this, attemptId);
        IdleTimeoutStreamingResponseHandler timeoutHandler = new IdleTimeoutStreamingResponseHandler(
                attemptHandler, scheduler, serialExecutor, modelId, idleTimeoutMs);
        timeoutHandler.start();
        try {
            delegate.doChat(request, timeoutHandler);
        } catch (RuntimeException e) {
            timeoutHandler.onError(e);
        }
    }

    /**
     * 处理回答分片
     */
    private void handlePartialResponse(long attemptId, String partialResponse) {
        if (!this.isCurrentAttempt(attemptId)) {
            return;
        }
        outputStarted = true;
        handler.onPartialResponse(partialResponse);
    }

    /**
     * 处理思考分片
     */
    private void handlePartialThinking(long attemptId, PartialThinking partialThinking) {
        if (!this.isCurrentAttempt(attemptId)) {
            return;
        }
        outputStarted = true;
        handler.onPartialThinking(partialThinking);
    }

    /**
     * 处理工具调用分片
     */
    private void handlePartialToolCall(long attemptId, PartialToolCall partialToolCall) {
        if (!this.isCurrentAttempt(attemptId)) {
            return;
        }
        outputStarted = true;
        handler.onPartialToolCall(partialToolCall);
    }

    /**
     * 处理完整工具调用
     */
    private void handleCompleteToolCall(long attemptId, CompleteToolCall completeToolCall) {
        if (!this.isCurrentAttempt(attemptId)) {
            return;
        }
        outputStarted = true;
        handler.onCompleteToolCall(completeToolCall);
    }

    /**
     * 处理最终完成事件
     */
    private void handleCompleteResponse(long attemptId, ChatResponse completeResponse) {
        if (!this.isCurrentAttempt(attemptId)) {
            return;
        }
        state = TERMINAL;
        this.cancelRetry();
        handler.onCompleteResponse(completeResponse);
    }

    /**
     * 处理异常并决定是否进入退避
     */
    private void handleError(long attemptId, Throwable error) {
        if (!this.isCurrentAttempt(attemptId)) {
            return;
        }
        ModelInvocationException exception = ModelErrorClassifier.wrap(error);
        if (!outputStarted && exception.isRetryable() && retryCount < maxRetries) {
            long nextAttempt = activeAttempt + 1;
            activeAttempt = nextAttempt;
            state = WAITING_RETRY;
            int nextRetryCount = retryCount + 1;
            long delayMs = retryPolicy.nextDelayMs(nextRetryCount);
            logger.info("model streaming retry scheduled, modelId={}, retryCount={}, delayMs={}, errorType={}",
                    modelId, nextRetryCount, delayMs, exception.getErrorType());
            retryFuture = scheduler.schedule(() -> serialExecutor.execute(() -> this.startAttempt(nextAttempt)),
                    delayMs, TimeUnit.MILLISECONDS);
            return;
        }
        state = TERMINAL;
        this.cancelRetry();
        logger.warn("model streaming invocation failed, modelId={}, retryCount={}, errorType={}",
                modelId, retryCount, exception.getErrorType());
        handler.onError(exception);
    }

    /**
     * 判断事件是否属于当前活跃尝试
     * @param attemptId 尝试id
     * @return 是否可处理
     */
    private boolean isCurrentAttempt(long attemptId) {
        return state == ACTIVE && activeAttempt == attemptId;
    }

    /**
     * 取消待执行退避任务
     */
    private void cancelRetry() {
        if (retryFuture == null) {
            return;
        }
        retryFuture.cancel(false);
        retryFuture = null;
    }
}
