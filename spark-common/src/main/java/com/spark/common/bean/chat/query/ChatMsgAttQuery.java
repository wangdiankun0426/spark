package com.spark.common.bean.chat.query;

import com.spark.common.bean.base.BaseQuery;
import lombok.Data;

import java.util.List;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-07-21 16:30:00
 * 聊天消息附件查询
 */
@Data
public class ChatMsgAttQuery extends BaseQuery {

    /**
     * 聊天消息id
     */
    private Long msgId;

    /**
     * 聊天消息id列表
     */
    private List<Long> msgIds;

    /**
     * 消息附件类型
     */
    private Integer msgAttType;
}
