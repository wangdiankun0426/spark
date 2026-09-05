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
 * @since 2025/11/3 21:52
 */
@Data
public class FlowInstanceDiscuss extends BaseEntity {

    /**
     * 流程实例节点id
     */
    private Long instanceNodeId;

    /**
     * 流程实例id
     */
    private Long instanceId;

    /**
     * 审批人id
     */
    private Long assigneeId;

    /**
     * 审批状态
     */
    private Integer status;

    /**
     * 审批意见
     */
    private String discuss;
}
