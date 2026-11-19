package com.spark.llm.model.fallback;

import com.spark.common.bean.base.SessionHolder;
import com.spark.common.bean.llm.dto.ModelInfo;
import com.spark.common.bean.llm.dto.UsageSnapshot;
import com.spark.common.llm.ModelUsageSceneHolder;
import com.spark.llm.model.circuit.ProviderCircuitBreaker;
import com.spark.llm.model.error.ModelErrorClassifier;
import com.spark.llm.model.error.ModelInvocationException;
import com.spark.llm.model.ratelimit.TenantRateLimiter;
import com.spark.llm.usage.ModelRetryContext;
import com.spark.llm.usage.ModelUsageRecorder;
import dev.langchain4j.model.ModelProvider;
import dev.langchain4j.model.chat.Capability;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.request.ChatRequestParameters;
import dev.langchain4j.model.chat.request.DefaultChatRequestParameters;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.output.TokenUsage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-11-19 09:00:00
 * 同步聊天模型有序降级外观
 */
public class FallbackChatModel implements ChatModel {
    private static final Logger logger = LoggerFactory.getLogger(FallbackChatModel.class);
    private final List<FallbackModelTarget<ChatModel>> targets;
    private final List<ChatModelListener> listeners;
    private final ProviderCircuitBreaker circuitBreaker;
    private final TenantRateLimiter rateLimiter;
    private final ModelUsageRecorder usageRecorder;

    public FallbackChatModel(List<FallbackModelTarget<ChatModel>> targets, List<ChatModelListener> listeners,
                             ProviderCircuitBreaker circuitBreaker, TenantRateLimiter rateLimiter,
                             ModelUsageRecorder usageRecorder) {
        if (targets == null || targets.isEmpty() || listeners == null || circuitBreaker == null
                || rateLimiter == null || usageRecorder == null) {
            throw new IllegalArgumentException("fallback chatModel targets, listeners, circuitBreaker, rateLimiter and usageRecorder must not be empty");
        }
        this.targets = List.copyOf(targets);
        this.listeners = List.copyOf(listeners);
        this.circuitBreaker = circuitBreaker;
        this.rateLimiter = rateLimiter;
        this.usageRecorder = usageRecorder;
    }

    @Override
    public ChatResponse doChat(ChatRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request must not be null");
        }
        Long tenantId = SessionHolder.getCurrentTenantId();
        if (!rateLimiter.tryAcquire(tenantId)) {
            this.recordRateLimited();
            throw TenantRateLimiter.rateLimitException();
        }
        ModelRetryContext context = ModelRetryContext.current();
        if (context == null) {
            context = new ModelRetryContext();
        }
        try {
            for (int index = 0; index < targets.size(); index++) {
                FallbackModelTarget<ChatModel> target = targets.get(index);
                ModelInfo modelInfo = target.getModelInfo();
                Long modelId = modelInfo.getModelId();
                Long providerId = modelInfo.getProviderId();
                if (!circuitBreaker.tryAcquire(providerId)) {
                    logger.warn("model fallback target circuit rejected, modelId={}, providerId={}", modelId, providerId);
                    continue;
                }
                context.selectTarget(modelInfo, index > 0);
                if (ModelRetryContext.current() != context) {
                    ModelRetryContext.bind(context);
                }
                try {
                    ChatModel delegate = target.getDelegate();
                    ChatResponse response = delegate.chat(request);
                    circuitBreaker.recordSuccess(providerId);
                    this.recordTokens(tenantId, response);
                    return response;
                } catch (RuntimeException e) {
                    ModelInvocationException exception = ModelErrorClassifier.wrap(e);
                    if (exception.isRetryable()) {
                        circuitBreaker.recordFailure(providerId);
                    }
                    if (index >= targets.size() - 1 || !ModelFallbackPolicy.shouldFallback(exception)) {
                        throw exception;
                    }
                    FallbackModelTarget<ChatModel> nextTarget = targets.get(index + 1);
                    ModelInfo nextModelInfo = nextTarget.getModelInfo();
                    Long nextModelId = nextModelInfo.getModelId();
                    logger.warn("model fallback target failed, modelId={}, nextModelId={}, errorType={}",
                            modelId, nextModelId, exception.getErrorType());
                } finally {
                    ModelRetryContext.unbind(context);
                }
            }
        } finally {
            ModelRetryContext.unbind(context);
            rateLimiter.release(tenantId);
        }
        throw ModelFallbackPolicy.circuitOpenException();
    }

    @Override
    public ChatRequestParameters defaultRequestParameters() {
        return DefaultChatRequestParameters.EMPTY;
    }

    @Override
    public List<ChatModelListener> listeners() {
        return listeners;
    }

    @Override
    public ModelProvider provider() {
        FallbackModelTarget<ChatModel> target = targets.get(0);
        ChatModel delegate = target.getDelegate();
        return delegate.provider();
    }

    @Override
    public Set<Capability> supportedCapabilities() {
        FallbackModelTarget<ChatModel> target = targets.get(0);
        ChatModel delegate = target.getDelegate();
        return delegate.supportedCapabilities();
    }

    /**
     * 采集一条被租户限流拒绝的用量
     */
    private void recordRateLimited() {
        FallbackModelTarget<ChatModel> rootTarget = targets.get(0);
        ModelInfo rootModelInfo = rootTarget.getModelInfo();
        UsageSnapshot snapshot = new UsageSnapshot(SessionHolder.getCurrentTenantId(), SessionHolder.getCurrentUserId(),
                SessionHolder.getCurrentDeptId(), ModelUsageSceneHolder.current().getValue());
        usageRecorder.recordRateLimited(rootModelInfo, snapshot);
    }

    /**
     * 成功调用后回填租户token计数
     * @param tenantId 租户id
     * @param response 模型响应
     */
    private void recordTokens(Long tenantId, ChatResponse response) {
        if (response == null) {
            return;
        }
        TokenUsage tokenUsage = response.tokenUsage();
        if (tokenUsage == null) {
            return;
        }
        Integer totalTokens = tokenUsage.totalTokenCount();
        rateLimiter.recordTokens(tenantId, totalTokens);
    }
}
