package com.spark.llm.model;

import dev.langchain4j.model.ModelProvider;
import dev.langchain4j.model.chat.Capability;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.request.ChatRequestParameters;
import dev.langchain4j.model.chat.response.ChatResponse;

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
 * 同步聊天模型统一调用包装
 */
public class InvokingChatModel implements ChatModel {
    private final ChatModel delegate;
    private final ModelInvoker modelInvoker;
    private final int maxRetries;

    public InvokingChatModel(ChatModel delegate, ModelInvoker modelInvoker, int maxRetries) {
        this.delegate = delegate;
        this.modelInvoker = modelInvoker;
        this.maxRetries = maxRetries;
    }

    @Override
    public ChatResponse doChat(ChatRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request must not be null");
        }
        return modelInvoker.invokeChat(() -> delegate.doChat(request), maxRetries);
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
