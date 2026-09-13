package com.spark.chat.service;

import com.spark.common.utils.JsonUtil;
import jakarta.websocket.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 * 在线用户和session的关系，同一账号可能多终端登录，因此一个用户对应多个session
 * @author wangdiankun
 * @since 2024/4/14 21:03
 */

public class UserChannelRel {
    private final static Logger logger = LoggerFactory.getLogger(UserChannelRel.class);

    /**
     * 用户id与该用户全部在线连接的关系，用Set保证同一连接重复注册不会重复推送
     */
    private static final ConcurrentHashMap<Long, Set<Session>> manage = new ConcurrentHashMap<>();

    /**
     * 注册连接，同一用户追加而不是覆盖
     * @param senderId 用户id
     * @param session websocket会话
     */
    public static void put(Long senderId, Session session) {
        if (senderId == null || session == null) {
            return;
        }
        manage.computeIfAbsent(senderId, key -> ConcurrentHashMap.newKeySet()).add(session);
    }

    /**
     * 获取该用户的全部在线连接
     * @param senderId 用户id
     * @return 连接快照，无在线连接时返回空集合
     */
    public static List<Session> get(Long senderId) {
        if (senderId == null) {
            return Collections.emptyList();
        }
        Set<Session> sessions = manage.get(senderId);
        if (sessions == null || sessions.isEmpty()) {
            return Collections.emptyList();
        }
        // 返回快照，避免广播遍历期间连接被并发移除
        return new ArrayList<>(sessions);
    }

    /**
     * 按连接移除，连接可能注册在任意用户下，逐个用户清理
     * @param session websocket会话
     */
    public static void removeBySession(Session session) {
        if (session == null) {
            return;
        }
        for (Long senderId : manage.keySet()) {
            // 移除连接与空集合删key合并为对一个key的原子操作，避免并发注册时误删刚加入的连接
            manage.computeIfPresent(senderId, (key, sessions) -> {
                sessions.remove(session);
                return sessions.isEmpty() ? null : sessions;
            });
        }
    }

    /**
     * 广播消息到多个连接，单个连接发送失败不影响其余连接
     * @param sessions 目标连接
     * @param dataContent 消息内容
     * @param excludeSession 需要排除的连接，消息发起端已本地插入不回推，可为空
     */
    public static void sendMessage(Collection<Session> sessions, Object dataContent, Session excludeSession) {
        if (sessions == null || sessions.isEmpty()) {
            return;
        }
        String excludeSessionId = excludeSession == null ? null : excludeSession.getId();
        // 多端广播只序列化一次
        String text = JsonUtil.toString(dataContent);
        for (Session session : sessions) {
            if (session == null) {
                continue;
            }
            if (excludeSessionId != null && excludeSessionId.equals(session.getId())) {
                continue;
            }
            if (!session.isOpen()) {
                // 服务端尚未感知的断线，顺手摘除避免后续继续做无用写入
                logger.warn("ws session not open, remove it, sessionId={}", session.getId());
                removeBySession(session);
                continue;
            }
            // 同一连接可能被多个流式回调同时写入，保持同步块避免帧交错
            synchronized (session) {
                try {
                    session.getBasicRemote().sendText(text);
                } catch (Exception e) {
                    // 单个连接发送失败不影响其余连接，同时视为连接不可用并摘除
                    logger.error("ws send message error, sessionId={}", session.getId(), e);
                    removeBySession(session);
                }
            }
        }
    }
}
