package com.spark.llm.model;

import com.spark.common.bean.base.SessionSnapshot;
import com.spark.common.utils.SessionContextUtil;
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
 * @since 2026-10-28 10:00:00
 * 流式回调包装，每个回调在HttpClient worker线程上恢复登录会话，工具内联执行与第二轮模型调用均带真实身份
 */
public class ContextStreamingResponseHandler implements StreamingChatResponseHandler {

    /**
     * 本次请求的登录会话快照
     */
    private final SessionSnapshot snapshot;

    /**
     * 业务回调处理器
     */
    private final StreamingChatResponseHandler delegate;

    public ContextStreamingResponseHandler(SessionSnapshot snapshot, StreamingChatResponseHandler delegate) {
        this.snapshot = snapshot;
        this.delegate = delegate;
    }

    @Override
    public void onPartialResponse(String partialResponse) {
        SessionContextUtil.runWith(snapshot, () -> delegate.onPartialResponse(partialResponse));
    }

    @Override
    public void onPartialThinking(PartialThinking partialThinking) {
        SessionContextUtil.runWith(snapshot, () -> delegate.onPartialThinking(partialThinking));
    }

    @Override
    public void onPartialToolCall(PartialToolCall partialToolCall) {
        SessionContextUtil.runWith(snapshot, () -> delegate.onPartialToolCall(partialToolCall));
    }

    @Override
    public void onCompleteToolCall(CompleteToolCall completeToolCall) {
        SessionContextUtil.runWith(snapshot, () -> delegate.onCompleteToolCall(completeToolCall));
    }

    @Override
    public void onCompleteResponse(ChatResponse completeResponse) {
        SessionContextUtil.runWith(snapshot, () -> delegate.onCompleteResponse(completeResponse));
    }

    @Override
    public void onError(Throwable error) {
        SessionContextUtil.runWith(snapshot, () -> delegate.onError(error));
    }
}
