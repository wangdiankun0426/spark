package com.spark.bean.chat.result;

import com.spark.bean.base.BaseResult;
import lombok.Data;

import java.util.List;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/4/15 13:17
 */
@Data
public class ChatMsgResult extends BaseResult {
    /**
     * 空间id
     */
    private Long spaceId;

    /**
     * 发送人
     */
    private Long senderId;

    /**
     * 发送人
     */
    private String senderName;

    /**
     * 接收人
     */
    private Long receiverId;

    /**
     * 接收人
     */
    private String receiverName;

    /**
     * 消息
     */
    private String message;

    /**
     * 签收状态 -1 未签收 1 已签收
     */
    private Integer signStatus = -1;

    /**
     * 签收状态
     */
    private String signStatusName;

    /**
     * 阅读状态 -1 未读 1 已读
     */
    private Integer readStatus = -1;

    /**
     * 阅读状态
     */
    private String readStatusName;

    /**
     * RAG参考文档列表
     */
    private List<ChatMsgAttResult> references;
}
