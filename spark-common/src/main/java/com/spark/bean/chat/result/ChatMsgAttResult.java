package com.spark.bean.chat.result;

import com.spark.bean.base.BaseResult;
import lombok.Data;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-07-21 16:30:00
 * 聊天消息附件
 */
@Data
public class ChatMsgAttResult extends BaseResult {

    /**
     * 聊天消息id
     */
    private Long msgId;

    /**
     * 消息附件类型
     */
    private Integer msgAttType;

    /**
     * 文档id
     */
    private Long docId;

    /**
     * 文档名称
     */
    private String docName;
}
