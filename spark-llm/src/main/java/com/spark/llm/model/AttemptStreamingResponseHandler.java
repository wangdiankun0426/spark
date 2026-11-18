package com.spark.llm.model;

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
 * @since 2026-11-18 09:00:00
 * 单次流式尝试的事件适配器，仅向逻辑调用协调器投递事件
 */
public class AttemptStreamingResponseHandler implements StreamingChatResponseHandler {
    private final RetryingStreamingInvocation invocation;
    private final long attemptId;

    public AttemptStreamingResponseHandler(RetryingStreamingInvocation invocation, long attemptId) {
        this.invocation = invocation;
        this.attemptId = attemptId;
    }

    @Override
    public void onPartialResponse(String partialResponse) {
        invocation.onPartialResponse(attemptId, partialResponse);
    }

    @Override
    public void onPartialThinking(PartialThinking partialThinking) {
        invocation.onPartialThinking(attemptId, partialThinking);
    }

    @Override
    public void onPartialToolCall(PartialToolCall partialToolCall) {
        invocation.onPartialToolCall(attemptId, partialToolCall);
    }

    @Override
    public void onCompleteToolCall(CompleteToolCall completeToolCall) {
        invocation.onCompleteToolCall(attemptId, completeToolCall);
    }

    @Override
    public void onCompleteResponse(ChatResponse completeResponse) {
        invocation.onCompleteResponse(attemptId, completeResponse);
    }

    @Override
    public void onError(Throwable error) {
        invocation.onError(attemptId, error);
    }
}
