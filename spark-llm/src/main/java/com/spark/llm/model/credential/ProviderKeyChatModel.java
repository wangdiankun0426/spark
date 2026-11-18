package com.spark.llm.model.credential;

import dev.langchain4j.model.ModelProvider;
import dev.langchain4j.model.chat.Capability;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.request.ChatRequestParameters;
import dev.langchain4j.model.chat.response.ChatResponse;

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
 * 按物理调用选择凭据的同步聊天模型
 */
public class ProviderKeyChatModel implements ChatModel {
    /**
     * 每次物理调用重新选择凭据和底层客户端的选择器
     */
    private final Supplier<ProviderKeyModel<ChatModel>> modelSelector;
    /**
     * 处理当前凭据限流冷却的路由器
     */
    private final ProviderKeyRouter providerKeyRouter;
    /**
     * 提供模型固定元数据的客户端，不参与实际调用
     */
    private final ChatModel metadataDelegate;
    /**
     * 逻辑调用级监听器，整个重试过程共享
     */
    private final List<ChatModelListener> listeners;

    public ProviderKeyChatModel(Supplier<ProviderKeyModel<ChatModel>> modelSelector,
                                ProviderKeyRouter providerKeyRouter, ChatModel metadataDelegate,
                                List<ChatModelListener> listeners) {
        if (modelSelector == null || providerKeyRouter == null || metadataDelegate == null || listeners == null) {
            throw new IllegalArgumentException("providerKey chatModel params must not be null");
        }
        this.modelSelector = modelSelector;
        this.providerKeyRouter = providerKeyRouter;
        this.metadataDelegate = metadataDelegate;
        this.listeners = listeners;
    }

    @Override
    public ChatResponse doChat(ChatRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request must not be null");
        }
        ProviderKeyModel<ChatModel> selected = modelSelector.get();
        try {
            ChatModel delegate = selected.getDelegate();
            return delegate.doChat(request);
        } catch (RuntimeException e) {
            Long providerId = selected.getProviderId();
            Long providerKeyId = selected.getProviderKeyId();
            providerKeyRouter.handleError(providerId, providerKeyId, e);
            throw e;
        }
    }

    @Override
    public ChatRequestParameters defaultRequestParameters() {
        return metadataDelegate.defaultRequestParameters();
    }

    @Override
    public List<ChatModelListener> listeners() {
        return listeners;
    }

    @Override
    public ModelProvider provider() {
        return metadataDelegate.provider();
    }

    @Override
    public Set<Capability> supportedCapabilities() {
        return metadataDelegate.supportedCapabilities();
    }
}
