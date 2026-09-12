package com.spark.common.bean.chat.query;

import com.spark.common.bean.base.BaseQuery;
import lombok.Data;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/4/15 13:19
 */
@Data
public class ChatMsgQuery extends BaseQuery {
    /**
     * 空间id
     */
    private Long spaceId;

    /**
     * 发送人
     */
    private Long senderId;

    /**
     * 接收人id
     */
    private Long receiverId;

    /**
     * 阅读状态 -1 未读 1 已读
     */
    private Integer readStatus;
}
