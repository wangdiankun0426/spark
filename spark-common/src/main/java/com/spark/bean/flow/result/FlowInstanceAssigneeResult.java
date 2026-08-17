package com.spark.bean.flow.result;

import com.spark.bean.base.BaseResult;
import lombok.Data;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2025/11/4 21:13
 */
@Data
public class FlowInstanceAssigneeResult extends BaseResult {

    /**
     * 流程实例id
     */
    private Long instanceId;

    /**
     * 流程实例节点id
     */
    private Long instanceNodeId;

    /**
     * 审批人集合id
     */
    private String assigneeSetId;

    /**
     * 审批人id
     */
    private Long assigneeId;

    /**
     * 审批状态
     */
    private Integer status;

    /**
     * 审批顺序
     */
    private Integer sort;

    /**
     * 流程实例id
     */
    private String flowableInstanceId;
}
