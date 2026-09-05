package com.spark.common.bean.chat.entity;

import com.spark.common.bean.base.BaseEntity;
import lombok.Data;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/4/14 20:54
 */
@Data
public class ChatMsg extends BaseEntity {

    /**
     * 空间id
     */
    private Long spaceId;

    /**
     * 发送人
     */
    private Long senderId;

    /**
     * 接收人
     */
    private Long receiverId;

    /**
     * 消息
     */
    private String message;

    /**
     * 签收状态 -1 未签收 1 已签收
     */
    private Integer signStatus = -1;

    /**
     * 阅读状态 -1 未读 1 已读
     */
    private Integer readStatus = -1;

    /**
     * 模型id
     */
    private Long modelId;
}
