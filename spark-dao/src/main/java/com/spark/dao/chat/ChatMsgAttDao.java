package com.spark.dao.chat;

import com.spark.bean.chat.entity.ChatMsgAtt;
import com.spark.bean.chat.query.ChatMsgAttQuery;
import com.spark.bean.chat.result.ChatMsgAttResult;
import com.spark.dao.BaseDao;

import java.util.List;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-07-21 16:30:00
 */
public interface ChatMsgAttDao extends BaseDao<ChatMsgAtt> {

    /**
     * 批量插入
     * @param list 附件列表
     * @return 插入条数
     */
    int batchInsert(List<ChatMsgAtt> list);

    /**
     * 查询列表
     * @param query 查询参数
     * @return 列表
     */
    List<ChatMsgAttResult> queryChatMsgAttList(ChatMsgAttQuery query);
}
