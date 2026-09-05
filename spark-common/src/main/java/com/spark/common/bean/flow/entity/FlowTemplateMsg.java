package com.spark.common.bean.flow.entity;

import com.spark.common.bean.base.BaseEntity;
import lombok.Data;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-08-06 10:00:00
 * 流程模板消息通知
 */
@Data
public class FlowTemplateMsg extends BaseEntity {

    /**
     * 流程模板id
     */
    private Long templateId;

    /**
     * 流程版本id
     */
    private Long revId;

    /**
     * 通知类型
     */
    private Integer type;

    /**
     * 是否启用
     */
    private Boolean enabled;

    /**
     * 通知内容模板
     */
    private String content;

    /**
     * 接收人表达式
     */
    private String recipient;

}