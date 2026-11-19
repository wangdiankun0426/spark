package com.spark.llm.model.fallback;

import com.spark.common.bean.llm.dto.ModelInfo;
import com.spark.common.enums.ModelErrorTypeEnum;
import com.spark.llm.model.circuit.ProviderCircuitBreaker;
import com.spark.llm.model.error.ModelErrorClassifier;
import com.spark.llm.model.error.ModelInvocationException;
import com.spark.llm.model.ratelimit.TenantRateLimiter;
import com.spark.llm.usage.ModelRetryContext;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.CompleteToolCall;
import dev.langchain4j.model.chat.response.PartialThinking;
import dev.langchain4j.model.chat.response.PartialToolCall;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.output.TokenUsage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-11-19 09:10:00
 * 单次流式逻辑调用的跨模型降级协调器
 */
public class FallbackStreamingInvocation {
    private static final Logger logger = LoggerFactory.getLogger(FallbackStreamingInvocation.class);
    private final List<FallbackModelTarget<StreamingChatModel>> targets;
    private final ChatRequest request;
    private final StreamingChatResponseHandler handler;
    private final ModelRetryContext context;
    private final ProviderCircuitBreaker circuitBreaker;
    private final TenantRateLimiter rateLimiter;
    private final Long tenantId;
    private int currentTargetIndex;
    private long activeAttempt;
    private boolean outputStarted;
    private boolean terminal;

    public FallbackStreamingInvocation(List<FallbackModelTarget<StreamingChatModel>> targets, ChatRequest request,
                                       StreamingChatResponseHandler handler, ModelRetryContext context,
                                       ProviderCircuitBreaker circuitBreaker, TenantRateLimiter rateLimiter, Long tenantId) {
        if (targets == null || targets.isEmpty() || request == null || handler == null || context == null
                || circuitBreaker == null || rateLimiter == null) {
            throw new IllegalArgumentException("fallback streaming invocation params must not be null");
        }
        this.targets = List.copyOf(targets);
        this.request = request;
        this.handler = handler;
        this.context = context;
        this.circuitBreaker = circuitBreaker;
        this.rateLimiter = rateLimiter;
        this.tenantId = tenantId;
    }

    /**
     * 启动首个模型目标
     */
    public synchronized void start() {
        this.startTarget(0);
    }

    /**
     * 处理回答分片
     * @param attemptId 目标尝试id
     * @param partialResponse 回答分片
     */
    public synchronized void onPartialResponse(long attemptId, String partialResponse) {
        if (!this.isCurrentAttempt(attemptId)) {
            return;
        }
        outputStarted = true;
        handler.onPartialResponse(partialResponse);
    }

    /**
     * 处理思考分片
     * @param attemptId 目标尝试id
     * @param partialThinking 思考分片
     */
    public synchronized void onPartialThinking(long attemptId, PartialThinking partialThinking) {
        if (!this.isCurrentAttempt(attemptId)) {
            return;
        }
        outputStarted = true;
        handler.onPartialThinking(partialThinking);
    }

    /**
     * 处理工具调用分片
     * @param attemptId 目标尝试id
     * @param partialToolCall 工具调用分片
     */
    public synchronized void onPartialToolCall(long attemptId, PartialToolCall partialToolCall) {
        if (!this.isCurrentAttempt(attemptId)) {
            return;
        }
        outputStarted = true;
        handler.onPartialToolCall(partialToolCall);
    }

    /**
     * 处理完整工具调用
     * @param attemptId 目标尝试id
     * @param completeToolCall 完整工具调用
     */
    public synchronized void onCompleteToolCall(long attemptId, CompleteToolCall completeToolCall) {
        if (!this.isCurrentAttempt(attemptId)) {
            return;
        }
        outputStarted = true;
        handler.onCompleteToolCall(completeToolCall);
    }

    /**
     * 处理当前目标完成
     * @param attemptId 目标尝试id
     * @param completeResponse 完成响应
     */
    public synchronized void onCompleteResponse(long attemptId, ChatResponse completeResponse) {
        if (!this.isCurrentAttempt(attemptId)) {
            return;
        }
        terminal = true;
        FallbackModelTarget<StreamingChatModel> currentTarget = targets.get(currentTargetIndex);
        ModelInfo currentModelInfo = currentTarget.getModelInfo();
        Long providerId = currentModelInfo.getProviderId();
        circuitBreaker.recordSuccess(providerId);
        this.recordTokens(completeResponse);
        rateLimiter.release(tenantId);
        handler.onCompleteResponse(completeResponse);
    }

    /**
     * 处理当前目标最终异常
     * @param attemptId 目标尝试id
     * @param error 异常
     */
    public synchronized void onError(long attemptId, Throwable error) {
        if (!this.isCurrentAttempt(attemptId)) {
            return;
        }
        ModelInvocationException exception = ModelErrorClassifier.wrap(error);
        FallbackModelTarget<StreamingChatModel> currentTarget = targets.get(currentTargetIndex);
        ModelInfo currentModelInfo = currentTarget.getModelInfo();
        Long currentModelId = currentModelInfo.getModelId();
        Long currentProviderId = currentModelInfo.getProviderId();
        if (exception.isRetryable()) {
            circuitBreaker.recordFailure(currentProviderId);
        }
        if (outputStarted || currentTargetIndex >= targets.size() - 1
                || !ModelFallbackPolicy.shouldFallback(exception)) {
            terminal = true;
            rateLimiter.release(tenantId);
            handler.onError(exception);
            return;
        }
        FallbackModelTarget<StreamingChatModel> nextTarget = targets.get(currentTargetIndex + 1);
        ModelInfo nextModelInfo = nextTarget.getModelInfo();
        Long nextModelId = nextModelInfo.getModelId();
        ModelErrorTypeEnum errorType = exception.getErrorType();
        logger.warn("model streaming fallback target failed, modelId={}, nextModelId={}, errorType={}",
                currentModelId, nextModelId, errorType);
        this.startTarget(currentTargetIndex + 1);
    }

    /**
     * 启动指定模型目标
     * @param targetIndex 目标下标
     */
    private void startTarget(int targetIndex) {
        if (terminal) {
            return;
        }
        int acquiredIndex = targetIndex;
        while (acquiredIndex < targets.size()) {
            FallbackModelTarget<StreamingChatModel> candidate = targets.get(acquiredIndex);
            ModelInfo candidateInfo = candidate.getModelInfo();
            Long candidateProviderId = candidateInfo.getProviderId();
            if (circuitBreaker.tryAcquire(candidateProviderId)) {
                break;
            }
            Long candidateModelId = candidateInfo.getModelId();
            logger.warn("model streaming fallback target circuit rejected, modelId={}, providerId={}",
                    candidateModelId, candidateProviderId);
            acquiredIndex++;
        }
        if (acquiredIndex >= targets.size()) {
            terminal = true;
            rateLimiter.release(tenantId);
            ModelInvocationException exception = ModelFallbackPolicy.circuitOpenException();
            handler.onError(exception);
            return;
        }
        currentTargetIndex = acquiredIndex;
        activeAttempt++;
        long attemptId = activeAttempt;
        FallbackModelTarget<StreamingChatModel> target = targets.get(acquiredIndex);
        ModelInfo modelInfo = target.getModelInfo();
        context.selectTarget(modelInfo, acquiredIndex > 0);
        FallbackStreamingResponseHandler targetHandler = new FallbackStreamingResponseHandler(this, attemptId);
        if (ModelRetryContext.current() != context) {
            ModelRetryContext.bind(context);
        }
        try {
            StreamingChatModel delegate = target.getDelegate();
            delegate.chat(request, targetHandler);
        } catch (RuntimeException e) {
            this.onError(attemptId, e);
        } finally {
            ModelRetryContext.unbind(context);
        }
    }

    /**
     * 判断事件是否属于当前目标且逻辑调用未终止
     * @param attemptId 目标尝试id
     * @return 是否可处理
     */
    private boolean isCurrentAttempt(long attemptId) {
        return !terminal && attemptId == activeAttempt;
    }

    /**
     * 成功完成后回填租户token计数
     * @param completeResponse 完成响应
     */
    private void recordTokens(ChatResponse completeResponse) {
        if (completeResponse == null) {
            return;
        }
        TokenUsage tokenUsage = completeResponse.tokenUsage();
        if (tokenUsage == null) {
            return;
        }
        Integer totalTokens = tokenUsage.totalTokenCount();
        rateLimiter.recordTokens(tenantId, totalTokens);
    }
}
