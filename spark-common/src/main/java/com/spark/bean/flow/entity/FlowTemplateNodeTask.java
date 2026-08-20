package com.spark.bean.flow.entity;

import com.spark.bean.base.BaseEntity;
import lombok.Data;

import java.util.List;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-08-20 15:00:00
 * 流程模板节点任务
 */
@Data
public class FlowTemplateNodeTask extends BaseEntity {

    /**
     * 流程模板id
     */
    private Long templateId;

    /**
     * 流程版本id
     */
    private Long revId;

    /**
     * 节点id
     */
    private String nodeId;

    /**
     * 任务模板id
     */
    private Long taskTemplateId;

    /**
     * 执行时机 FlowNodeTaskExecuteEnum
     */
    private Integer executeType;

    /**
     * 执行顺序
     */
    private Integer sort;

    /**
     * 任务参数列表（非表字段，保存时随任务一并落库）
     */
    private List<FlowTemplateNodeTaskParam> params;

}
