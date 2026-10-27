package com.spark.llm.model;

import com.spark.common.bean.base.SessionSnapshot;
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
 * @since 2026-10-28 10:00:00
 * 携带登录会话快照的流式模型包装，每次请求新建、底层模型仍用跨用户共享单例
 */
public class ContextStreamingChatModel implements StreamingChatModel {

    /**
     * 被包装的缓存流式模型
     */
    private final StreamingChatModel delegate;

    /**
     * 本次请求的登录会话快照
     */
    private final SessionSnapshot snapshot;

    public ContextStreamingChatModel(StreamingChatModel delegate, SessionSnapshot snapshot) {
        this.delegate = delegate;
        this.snapshot = snapshot;
    }

    @Override
    public void chat(ChatRequest request, StreamingChatResponseHandler handler) {
        delegate.chat(request, new ContextStreamingResponseHandler(snapshot, handler));
    }

    @Override
    public void doChat(ChatRequest request, StreamingChatResponseHandler handler) {
        this.chat(request, handler);
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
