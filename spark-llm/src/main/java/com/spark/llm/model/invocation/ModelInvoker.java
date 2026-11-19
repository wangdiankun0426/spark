package com.spark.llm.model.invocation;

import com.spark.llm.model.error.ModelErrorClassifier;
import com.spark.llm.model.error.ModelInvocationException;
import com.spark.llm.model.error.ModelRetryPolicy;
import com.spark.llm.model.streaming.RetryingStreamingInvocation;

import com.spark.llm.usage.ModelRetryContext;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.scoring.ScoringModel;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * 模型统一调用层，负责模型包装、自动重试与流式空闲超时调度
 */
@Component
public class ModelInvoker {
    private static final Logger logger = LoggerFactory.getLogger(ModelInvoker.class);
    public static final int DEFAULT_TIMEOUT_MS = 60000;
    public static final int MIN_TIMEOUT_MS = 1000;
    public static final int MAX_TIMEOUT_MS = 600000;
    private final ScheduledThreadPoolExecutor scheduler;
    private final ExecutorService callbackExecutor;
    private final AtomicLong callbackThreadSequence = new AtomicLong();
    private final ModelRetryPolicy retryPolicy;

    public ModelInvoker() {
        this.retryPolicy = new ModelRetryPolicy();
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

    public ModelInvoker(ScheduledThreadPoolExecutor scheduler, ExecutorService callbackExecutor,
                        ModelRetryPolicy retryPolicy) {
        if (scheduler == null || callbackExecutor == null || retryPolicy == null) {
            throw new IllegalArgumentException("modelInvoker params must not be null");
        }
        this.scheduler = scheduler;
        this.callbackExecutor = callbackExecutor;
        this.retryPolicy = retryPolicy;
    }

    /**
     * 包装同步聊天模型，默认不重试
     * @param delegate 原模型
     * @return 统一调用模型
     */
    public ChatModel wrap(ChatModel delegate) {
        return this.wrap(delegate, 0);
    }

    /**
     * 包装同步聊天模型
     * @param delegate 原模型
     * @param maxRetries 额外重试次数
     * @return 统一调用模型
     */
    public ChatModel wrap(ChatModel delegate, int maxRetries) {
        if (delegate == null) {
            throw new IllegalArgumentException("chatModel must not be null");
        }
        ModelRetryPolicy.resolveMaxRetries(maxRetries);
        return new InvokingChatModel(delegate, this, maxRetries);
    }

    /**
     * 包装流式聊天模型，默认不重试
     * @param delegate 原模型
     * @param modelId 模型id
     * @param idleTimeoutMs 流式空闲超时毫秒
     * @return 统一调用模型
     */
    public StreamingChatModel wrap(StreamingChatModel delegate, Long modelId, long idleTimeoutMs) {
        return this.wrap(delegate, modelId, idleTimeoutMs, 0);
    }

    /**
     * 包装流式聊天模型
     * @param delegate 原模型
     * @param modelId 模型id
     * @param idleTimeoutMs 流式空闲超时毫秒
     * @param maxRetries 额外重试次数
     * @return 统一调用模型
     */
    public StreamingChatModel wrap(StreamingChatModel delegate, Long modelId, long idleTimeoutMs, int maxRetries) {
        if (delegate == null || modelId == null) {
            throw new IllegalArgumentException("streamingChatModel and modelId must not be null");
        }
        if (idleTimeoutMs < MIN_TIMEOUT_MS || idleTimeoutMs > MAX_TIMEOUT_MS) {
            throw new IllegalArgumentException("idleTimeoutMs is out of range");
        }
        ModelRetryPolicy.resolveMaxRetries(maxRetries);
        return new InvokingStreamingChatModel(delegate, this, modelId, idleTimeoutMs, maxRetries);
    }

    /**
     * 包装向量模型，默认不重试
     * @param delegate 原模型
     * @return 统一调用模型
     */
    public EmbeddingModel wrap(EmbeddingModel delegate) {
        return this.wrap(delegate, 0);
    }

    /**
     * 包装向量模型
     * @param delegate 原模型
     * @param maxRetries 额外重试次数
     * @return 统一调用模型
     */
    public EmbeddingModel wrap(EmbeddingModel delegate, int maxRetries) {
        if (delegate == null) {
            throw new IllegalArgumentException("embeddingModel must not be null");
        }
        ModelRetryPolicy.resolveMaxRetries(maxRetries);
        return new InvokingEmbeddingModel(delegate, this, maxRetries);
    }

    /**
     * 包装排序模型，默认不重试
     * @param delegate 原模型
     * @return 统一调用模型
     */
    public ScoringModel wrap(ScoringModel delegate) {
        return this.wrap(delegate, 0);
    }

    /**
     * 包装排序模型
     * @param delegate 原模型
     * @param maxRetries 额外重试次数
     * @return 统一调用模型
     */
    public ScoringModel wrap(ScoringModel delegate, int maxRetries) {
        if (delegate == null) {
            throw new IllegalArgumentException("scoringModel must not be null");
        }
        ModelRetryPolicy.resolveMaxRetries(maxRetries);
        return new InvokingScoringModel(delegate, this, maxRetries);
    }

    /**
     * 执行同步模型调用，默认不重试
     * @param invocation 调用逻辑
     * @return 调用结果
     * @param <T> 结果类型
     */
    public <T> T invoke(Supplier<T> invocation) {
        return this.invoke(invocation, 0);
    }

    /**
     * 执行带自动重试的同步模型调用
     * @param invocation 调用逻辑
     * @param maxRetries 额外重试次数
     * @return 调用结果
     * @param <T> 结果类型
     */
    public <T> T invoke(Supplier<T> invocation, int maxRetries) {
        if (invocation == null) {
            throw new IllegalArgumentException("invocation must not be null");
        }
        ModelRetryPolicy.resolveMaxRetries(maxRetries);
        ModelRetryContext retryContext = this.bindRetryContext();
        return this.invokeWithRetry(invocation, maxRetries, retryContext);
    }

    /**
     * 执行带自动重试的同步聊天调用
     * @param invocation 调用逻辑
     * @param maxRetries 额外重试次数
     * @return 调用结果
     * @param <T> 结果类型
     */
    public <T> T invokeChat(Supplier<T> invocation, int maxRetries) {
        if (invocation == null) {
            throw new IllegalArgumentException("invocation must not be null");
        }
        ModelRetryPolicy.resolveMaxRetries(maxRetries);
        ModelRetryContext retryContext = this.bindRetryContext();
        return this.invokeWithRetry(invocation, maxRetries, retryContext);
    }

    /**
     * 执行流式模型调用，默认不重试
     * @param delegate 原模型
     * @param request 聊天请求
     * @param handler 业务回调
     * @param modelId 模型id
     * @param idleTimeoutMs 流式空闲超时毫秒
     */
    public void invoke(StreamingChatModel delegate, ChatRequest request, StreamingChatResponseHandler handler,
                       Long modelId, long idleTimeoutMs) {
        this.invoke(delegate, request, handler, modelId, idleTimeoutMs, 0);
    }

    /**
     * 执行带首输出前重试的流式模型调用
     * @param delegate 原模型
     * @param request 聊天请求
     * @param handler 业务回调
     * @param modelId 模型id
     * @param idleTimeoutMs 流式空闲超时毫秒
     * @param maxRetries 额外重试次数
     */
    public void invoke(StreamingChatModel delegate, ChatRequest request, StreamingChatResponseHandler handler,
                       Long modelId, long idleTimeoutMs, int maxRetries) {
        if (delegate == null || request == null || handler == null || modelId == null) {
            throw new IllegalArgumentException("streaming invocation params must not be null");
        }
        ModelRetryPolicy.resolveMaxRetries(maxRetries);
        ModelRetryContext retryContext = this.bindRetryContext();
        try {
            RetryingStreamingInvocation invocation = new RetryingStreamingInvocation(delegate, request, handler,
                    scheduler, callbackExecutor, modelId, idleTimeoutMs, maxRetries, retryPolicy, retryContext);
            invocation.start();
        } finally {
            ModelRetryContext.unbind(retryContext);
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
     * 解析额外重试次数
     * @param maxRetries 配置次数
     * @return 有效次数
     */
    public int resolveMaxRetries(Integer maxRetries) {
        return ModelRetryPolicy.resolveMaxRetries(maxRetries);
    }

    /**
     * 关闭共享调度器
     */
    @PreDestroy
    public void destroy() {
        scheduler.shutdownNow();
        callbackExecutor.shutdownNow();
    }

    /**
     * 执行同步重试循环
     * @param invocation 调用逻辑
     * @param maxRetries 额外重试次数
     * @param retryContext 重试上下文
     * @return 调用结果
     * @param <T> 结果类型
     */
    private <T> T invokeWithRetry(Supplier<T> invocation, int maxRetries, ModelRetryContext retryContext) {
        try {
            int retryCount = 0;
            while (true) {
                try {
                    return invocation.get();
                } catch (RuntimeException e) {
                    ModelInvocationException exception = ModelErrorClassifier.wrap(e);
                    if (!exception.isRetryable() || retryCount >= maxRetries) {
                        logger.warn("model invocation failed, retryCount={}, errorType={}",
                                retryCount, exception.getErrorType());
                        throw exception;
                    }
                    int nextRetryCount = retryCount + 1;
                    Long retryAfterMs = exception.getRetryAfterMs();
                    long delayMs = retryPolicy.nextDelayMs(nextRetryCount, retryAfterMs);
                    logger.info("model invocation retry scheduled, retryCount={}, delayMs={}, errorType={}",
                            nextRetryCount, delayMs, exception.getErrorType());
                    this.waitBeforeRetry(delayMs, exception);
                    retryCount = retryContext.incrementRetryCount();
                }
            }
        } finally {
            ModelRetryContext.unbind(retryContext);
        }
    }

    /**
     * 获取或创建当前逻辑调用的重试上下文
     * @return 重试上下文
     */
    private ModelRetryContext bindRetryContext() {
        ModelRetryContext retryContext = ModelRetryContext.current();
        if (retryContext != null) {
            return retryContext;
        }
        ModelRetryContext newContext = new ModelRetryContext();
        ModelRetryContext.bind(newContext);
        return newContext;
    }

    /**
     * 同步等待下一次重试
     * @param delayMs 等待毫秒
     * @param exception 最后一次模型异常
     */
    private void waitBeforeRetry(long delayMs, ModelInvocationException exception) {
        if (Thread.currentThread().isInterrupted()) {
            InterruptedException interruptedException = new InterruptedException("model retry interrupted");
            exception.addSuppressed(interruptedException);
            throw exception;
        }
        if (delayMs <= 0) {
            return;
        }
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            exception.addSuppressed(e);
            throw exception;
        }
    }
}
