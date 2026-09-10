package com.spark.common.bean.chat.result;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.sql.Timestamp;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-09-11 10:20:00
 * AI会话结果
 */
@Data
public class AiChatSpaceResult extends ChatSpaceResult {

    /**
     * 接收对象名称，语言模型或智能体
     */
    private String modelName;

    /**
     * 最近一条消息
     */
    private String lastMessage;

    /**
     * 最近一条消息时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private Timestamp lastTime;
}
