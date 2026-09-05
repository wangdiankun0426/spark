package com.spark.common.bean.flow.result;

import com.spark.common.bean.base.BaseResult;
import lombok.Data;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2025/11/3 22:06
 */
@Data
public class FlowInstanceDiscussResult extends BaseResult {

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

    /**
     * 审批人名称
     */
    private String assigneeName;

    /**
     * 审批状态名称
     */
    private String statusName;
}
