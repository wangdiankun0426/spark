package com.spark.config.wecom.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-07-28 14:00:00
 * 企业微信用户列表响应
 */
@Data
public class WeComUserRes {

    /**
     * 错误码（0表示成功）
     */
    private Integer errcode;

    /**
     * 错误信息
     */
    private String errmsg;

    /**
     * 用户id
     */
    private String userid;


    /**
     * 成员票据
     */
    private String user_ticket;

    /**
     * 文档检索票据
     */
    private String user_doc_ticket;
}