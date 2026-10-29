package com.spark.llm.memory;

import com.spark.common.constant.ObjectCacheKey;
import com.spark.common.utils.StringUtil;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageDeserializer;
import dev.langchain4j.data.message.ChatMessageSerializer;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026/07/19 14:30:00
 * 基于Redis的会话记忆存储，按memoryId隔离对话历史
 */
@Component
public class RedisChatMemory implements ChatMemoryStore {
    private final static Logger logger = LoggerFactory.getLogger(RedisChatMemory.class);
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 构造redis key
     * @param memoryId 会话id
     * @return redis key
     */
    private String buildKey(Object memoryId) {
        return ObjectCacheKey.LLM_CHAT_MEMORY + memoryId;
    }

    /**
     * 查询会话消息列表
     * @param memoryId 会话id
     * @return 消息列表
     */
    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        if (memoryId == null) {
            return new ArrayList<>();
        }
        try {
            String json = stringRedisTemplate.opsForValue().get(buildKey(memoryId));
            if (StringUtil.isBlank(json)) {
                return new ArrayList<>();
            }
            return ChatMessageDeserializer.messagesFromJson(json);
        } catch (Exception e) {
            logger.error("get chat memory from redis fail, memoryId={}", memoryId, e);
            return new ArrayList<>();
        }
    }

    /**
     * 更新会话消息
     * @param memoryId 会话id
     * @param messages 消息列表
     */
    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {
        if (memoryId == null) {
            return;
        }
        try {
            String json = ChatMessageSerializer.messagesToJson(messages);
            stringRedisTemplate.opsForValue().set(buildKey(memoryId), json);
        } catch (Exception e) {
            logger.error("save chat memory to redis fail, memoryId={}", memoryId, e);
        }
    }

    /**
     * 删除会话消息
     * @param memoryId 会话id
     */
    @Override
    public void deleteMessages(Object memoryId) {
        if (memoryId == null) {
            return;
        }
        try {
            stringRedisTemplate.delete(buildKey(memoryId));
        } catch (Exception e) {
            logger.error("delete chat memory from redis fail, memoryId={}", memoryId, e);
        }
    }
}
