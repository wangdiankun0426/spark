package com.spark.bean.chat.vo;

import com.spark.bean.base.BaseVO;
import com.spark.bean.chat.entity.ChatMsg;
import com.spark.bean.chat.result.ChatMsgAttResult;
import lombok.Data;

import java.util.List;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/4/14 20:53
 */
@Data
public class DataContentVO extends BaseVO {

    /**
     * 动作类型 MsgActionEnum
     */
    private Integer action;

    /**
     * 消息体
     */
    private ChatMsg chatMsg;

    /**
     * RAG参考文档附件列表
     */
    private List<ChatMsgAttResult> references;

}
