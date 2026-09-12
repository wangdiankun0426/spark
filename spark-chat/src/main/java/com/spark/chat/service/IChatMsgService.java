package com.spark.chat.service;

import com.spark.common.bean.base.PageResult;
import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.chat.entity.ChatMsg;
import com.spark.common.bean.chat.query.ChatMsgQuery;
import com.spark.common.bean.chat.result.ChatMsgResult;
import com.spark.common.bean.chat.vo.ChatMsgVO;
import jakarta.websocket.Session;

import java.util.List;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/4/14 21:58
 */
public interface IChatMsgService {

    /**
     * 创建聊天消息
     * @param chatMsgVO
     * @return
     */
    ResultData<ChatMsg> createChatMsg(ChatMsgVO chatMsgVO);

    /**
     * 创建agent消息
     * @param chatMsgVO
     * @return
     */
    void createAgentChatMsg(ChatMsgVO chatMsgVO, Session findSession);

    /**
     * 创建model消息
     * @param chatMsgVO
     * @return
     */
    void createModelChatMsg(ChatMsgVO chatMsgVO, Session findSession);

    /**
     * 签收聊天消息
     * @param chatMsgVO
     * @return
     */
    ResultData<Void> signChatMsg(ChatMsgVO chatMsgVO);

    /**
     * 阅读聊天消息
     * @param chatMsgVO
     * @return
     */
    ResultData<Void> readChatMsg(ChatMsgVO chatMsgVO);

    /**
     * 查询未读消息列表
     * @param query
     * @return
     */
    ResultData<List<ChatMsgResult>> queryNoReadChatMsgList(ChatMsgQuery query);

    /**
     * 分页查询列表
     * @param query
     * @return
     */
    ResultData<PageResult<ChatMsgResult>> pageChatMsgList(ChatMsgQuery query);
}
