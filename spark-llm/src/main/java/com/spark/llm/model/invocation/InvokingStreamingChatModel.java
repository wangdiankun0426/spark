package com.spark.llm.model.invocation;

import dev.langchain4j.model.ModelProvider;
import dev.langchain4j.model.chat.Capability;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.request.ChatRequestParameters;
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
 * @since 2026-11-17 10:00:00
 * 流式聊天模型统一调用包装
 */
public class InvokingStreamingChatModel implements StreamingChatModel {
    private final StreamingChatModel delegate;
    private final ModelInvoker modelInvoker;
    private final Long modelId;
    private final long idleTimeoutMs;
    private final int maxRetries;

    public InvokingStreamingChatModel(StreamingChatModel delegate, ModelInvoker modelInvoker,
                                      Long modelId, long idleTimeoutMs, int maxRetries) {
        this.delegate = delegate;
        this.modelInvoker = modelInvoker;
        this.modelId = modelId;
        this.idleTimeoutMs = idleTimeoutMs;
        this.maxRetries = maxRetries;
    }

    @Override
    public void doChat(ChatRequest request, StreamingChatResponseHandler handler) {
        if (request == null || handler == null) {
            throw new IllegalArgumentException("request and handler must not be null");
        }
        modelInvoker.invoke(delegate, request, handler, modelId, idleTimeoutMs, maxRetries);
    }

    @Override
    public ChatRequestParameters defaultRequestParameters() {
        return delegate.defaultRequestParameters();
    }

    @Override
    public List<ChatModelListener> listeners() {
        return delegate.listeners();
    }

    @Override
    public ModelProvider provider() {
        return delegate.provider();
    }

    @Override
    public Set<Capability> supportedCapabilities() {
        return delegate.supportedCapabilities();
    }
}
