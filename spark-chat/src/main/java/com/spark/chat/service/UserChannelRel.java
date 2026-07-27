package com.spark.chat.service;

import jakarta.websocket.Session;

import java.util.concurrent.ConcurrentHashMap;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 * 在线用户和session的关系
 * @author wangdiankun
 * @since 2024/4/14 21:03
 */

public class UserChannelRel {
    private static ConcurrentHashMap<Long, Session> manage = new ConcurrentHashMap<>();

    public static void put(Long senderId, Session session) {
        manage.put(senderId, session);
    }

    public static Session get(Long senderId) {
        return manage.get(senderId);
    }

    public static void removeBySession(Session session) {
        // 遍历map，找到并移除对应的session
        manage.entrySet().removeIf(entry -> entry.getValue().equals(session));
    }

}
