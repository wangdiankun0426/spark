package com.spark.llm;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;

/**
 * Agent接口定义
 * 支持同步对话和流式对话（含工具调用过程通知）
 *
 * @author wangdiankun
 * @since 2025/10/19 19:23
 */
public interface IAgent {

    /**
     * 同步对话
     * @param memoryId 会话记忆id（用于隔离不同会话的记忆）
     * @param userMessage 用户消息
     * @return AI回复内容
     */
    String chat(@MemoryId Object memoryId, @UserMessage String userMessage);

    /**
     * 流式对话（含工具调用过程通知）
     * 通过TokenStream的onPartialResponse/onToolExecuted等回调感知工具调用过程
     * @param memoryId 会话记忆id（用于隔离不同会话的记忆）
     * @param userMessage 用户消息
     * @return TokenStream流式响应
     */
    TokenStream streamChat(@MemoryId Object memoryId, @UserMessage String userMessage);
}
