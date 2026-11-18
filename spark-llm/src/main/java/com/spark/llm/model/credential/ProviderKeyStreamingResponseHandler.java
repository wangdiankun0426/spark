package com.spark.llm.model.credential;

import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.CompleteToolCall;
import dev.langchain4j.model.chat.response.PartialThinking;
import dev.langchain4j.model.chat.response.PartialToolCall;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-11-18 10:00:00
 * 流式凭据错误处理与业务事件转发器
 */
public class ProviderKeyStreamingResponseHandler implements StreamingChatResponseHandler {
    /**
     * 接收转发事件的下游流式处理器
     */
    private final StreamingChatResponseHandler delegate;
    /**
     * 处理当前凭据限流冷却的路由器
     */
    private final ProviderKeyRouter providerKeyRouter;
    /**
     * 当前流式物理尝试使用的厂商id
     */
    private final Long providerId;
    /**
     * 当前流式物理尝试使用的凭据id
     */
    private final Long providerKeyId;

    public ProviderKeyStreamingResponseHandler(StreamingChatResponseHandler delegate,
                                               ProviderKeyRouter providerKeyRouter,
                                               Long providerId, Long providerKeyId) {
        if (delegate == null || providerKeyRouter == null || providerId == null || providerKeyId == null) {
            throw new IllegalArgumentException("providerKey streaming handler params must not be null");
        }
        this.delegate = delegate;
        this.providerKeyRouter = providerKeyRouter;
        this.providerId = providerId;
        this.providerKeyId = providerKeyId;
    }

    @Override
    public void onPartialResponse(String partialResponse) {
        delegate.onPartialResponse(partialResponse);
    }

    @Override
    public void onPartialThinking(PartialThinking partialThinking) {
        delegate.onPartialThinking(partialThinking);
    }

    @Override
    public void onPartialToolCall(PartialToolCall partialToolCall) {
        delegate.onPartialToolCall(partialToolCall);
    }

    @Override
    public void onCompleteToolCall(CompleteToolCall completeToolCall) {
        delegate.onCompleteToolCall(completeToolCall);
    }

    @Override
    public void onCompleteResponse(ChatResponse completeResponse) {
        delegate.onCompleteResponse(completeResponse);
    }

    @Override
    public void onError(Throwable error) {
        providerKeyRouter.handleError(providerId, providerKeyId, error);
        delegate.onError(error);
    }
}
