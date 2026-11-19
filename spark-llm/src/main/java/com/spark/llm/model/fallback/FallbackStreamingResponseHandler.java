package com.spark.llm.model.fallback;

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
 * @since 2026-11-19 09:10:00
 * 降级链单目标流式事件适配器
 */
public class FallbackStreamingResponseHandler implements StreamingChatResponseHandler {
    private final FallbackStreamingInvocation invocation;
    private final long attemptId;

    public FallbackStreamingResponseHandler(FallbackStreamingInvocation invocation, long attemptId) {
        if (invocation == null) {
            throw new IllegalArgumentException("invocation must not be null");
        }
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
