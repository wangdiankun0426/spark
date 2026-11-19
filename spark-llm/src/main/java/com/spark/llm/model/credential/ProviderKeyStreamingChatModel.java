package com.spark.llm.model.credential;

import dev.langchain4j.model.ModelProvider;
import dev.langchain4j.model.chat.Capability;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.request.ChatRequestParameters;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-11-18 10:00:00
 * 按物理调用选择凭据的流式聊天模型
 */
public class ProviderKeyStreamingChatModel implements StreamingChatModel {
    /**
     * 每次流式物理调用重新选择凭据和底层客户端的选择器
     */
    private final Supplier<ProviderKeyModel<StreamingChatModel>> modelSelector;
    /**
     * 处理当前凭据限流冷却的路由器
     */
    private final ProviderKeyRouter providerKeyRouter;
    /**
     * 当前流式模型的默认请求参数
     */
    private final ChatRequestParameters defaultRequestParameters;
    /**
     * 逻辑流式调用级监听器，整个重试过程共享
     */
    private final List<ChatModelListener> listeners;

    public ProviderKeyStreamingChatModel(Supplier<ProviderKeyModel<StreamingChatModel>> modelSelector,
                                         ProviderKeyRouter providerKeyRouter,
                                         ChatRequestParameters defaultRequestParameters,
                                         List<ChatModelListener> listeners) {
        if (modelSelector == null || providerKeyRouter == null || defaultRequestParameters == null || listeners == null) {
            throw new IllegalArgumentException("providerKey streaming chatModel params must not be null");
        }
        this.modelSelector = modelSelector;
        this.providerKeyRouter = providerKeyRouter;
        this.defaultRequestParameters = defaultRequestParameters;
        this.listeners = listeners;
    }

    @Override
    public void doChat(ChatRequest request, StreamingChatResponseHandler handler) {
        if (request == null || handler == null) {
            throw new IllegalArgumentException("request and handler must not be null");
        }
        ProviderKeyModel<StreamingChatModel> selected = modelSelector.get();
        Long providerId = selected.getProviderId();
        Long providerKeyId = selected.getProviderKeyId();
        ProviderKeyStreamingResponseHandler keyHandler = new ProviderKeyStreamingResponseHandler(
                handler, providerKeyRouter, providerId, providerKeyId);
        try {
            StreamingChatModel delegate = selected.getDelegate();
            delegate.doChat(request, keyHandler);
        } catch (RuntimeException e) {
            providerKeyRouter.handleError(providerId, providerKeyId, e);
            throw e;
        }
    }

    @Override
    public ChatRequestParameters defaultRequestParameters() {
        return defaultRequestParameters;
    }

    @Override
    public List<ChatModelListener> listeners() {
        return listeners;
    }

    @Override
    public ModelProvider provider() {
        return ModelProvider.OPEN_AI;
    }

    @Override
    public Set<Capability> supportedCapabilities() {
        return Set.of();
    }
}
