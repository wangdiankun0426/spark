package com.spark.llm.model.fallback;

import com.spark.llm.model.circuit.ProviderCircuitBreaker;
import com.spark.llm.usage.ModelRetryContext;
import dev.langchain4j.model.ModelProvider;
import dev.langchain4j.model.chat.Capability;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.request.ChatRequestParameters;
import dev.langchain4j.model.chat.request.DefaultChatRequestParameters;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;

import java.util.List;
import java.util.Set;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-11-19 09:10:00
 * 流式聊天模型有序降级外观
 */
public class FallbackStreamingChatModel implements StreamingChatModel {
    private final List<FallbackModelTarget<StreamingChatModel>> targets;
    private final List<ChatModelListener> listeners;
    private final ProviderCircuitBreaker circuitBreaker;

    public FallbackStreamingChatModel(List<FallbackModelTarget<StreamingChatModel>> targets,
                                      List<ChatModelListener> listeners, ProviderCircuitBreaker circuitBreaker) {
        if (targets == null || targets.isEmpty() || listeners == null || circuitBreaker == null) {
            throw new IllegalArgumentException("fallback streaming targets, listeners and circuitBreaker must not be null");
        }
        this.targets = List.copyOf(targets);
        this.listeners = List.copyOf(listeners);
        this.circuitBreaker = circuitBreaker;
    }

    @Override
    public void doChat(ChatRequest request, StreamingChatResponseHandler handler) {
        if (request == null || handler == null) {
            throw new IllegalArgumentException("request and handler must not be null");
        }
        ModelRetryContext context = ModelRetryContext.current();
        if (context == null) {
            context = new ModelRetryContext();
        }
        try {
            FallbackStreamingInvocation invocation = new FallbackStreamingInvocation(
                    targets, request, handler, context, circuitBreaker);
            invocation.start();
        } finally {
            ModelRetryContext.unbind(context);
        }
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
        FallbackModelTarget<StreamingChatModel> target = targets.get(0);
        StreamingChatModel delegate = target.getDelegate();
        return delegate.provider();
    }

    @Override
    public Set<Capability> supportedCapabilities() {
        FallbackModelTarget<StreamingChatModel> target = targets.get(0);
        StreamingChatModel delegate = target.getDelegate();
        return delegate.supportedCapabilities();
    }
}
