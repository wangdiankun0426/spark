package com.spark.dao.chat;

import com.spark.common.bean.chat.entity.ChatMsg;
import com.spark.common.bean.chat.query.ChatMsgQuery;
import com.spark.common.bean.chat.result.ChatMsgResult;
import com.spark.dao.BaseDao;

import java.util.List;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/4/15 13:10
 */
public interface ChatMsgDao extends BaseDao<ChatMsg> {
    /**
     * 插入数据
     * @param chatMsg
     * @return
     */
    @Override
    int insert(ChatMsg chatMsg);

    /**
     * 根据id修改
     * @param chatMsg
     * @return
     */
    @Override
    int updateById(ChatMsg chatMsg);

    /**
     * 查询单条
     * @param query
     * @return
     */
    ChatMsgResult queryChatMsg(ChatMsgQuery query);

    /**
     * 查询列表
     * @param query
     * @return
     */
    List<ChatMsgResult> queryChatMsgList(ChatMsgQuery query);

    /**
     * 查询数量
     * @param query
     * @return
     */
    int queryChatMsgCount(ChatMsgQuery query);
}
