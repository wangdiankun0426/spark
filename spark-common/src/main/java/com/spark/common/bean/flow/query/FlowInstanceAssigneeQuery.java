package com.spark.common.bean.flow.query;

import com.spark.common.bean.base.BaseQuery;
import lombok.Data;

import java.util.List;

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
public class FlowInstanceAssigneeQuery extends BaseQuery {

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
     * 审批状态列表
     */
    private List<Integer> statuses;

}
