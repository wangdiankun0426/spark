package com.spark.bean.flow.entity;

import com.spark.bean.base.BaseEntity;
import lombok.Data;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2025/11/2 16:42
 */
@Data
public class FlowTemplateSequence extends BaseEntity {

    /**
     * 流程模板id
     */
    private Long templateId;

    /**
     * 流程版本id
     */
    private Long revId;

    /**
     * 连线id
     */
    private String sequenceId;

    /**
     * 连线起点
     */
    private String sourceRef;

    /**
     * 连线终点
     */
    private String targetRef;

    /**
     * 条件表达式
     */
    private String condition;

}
