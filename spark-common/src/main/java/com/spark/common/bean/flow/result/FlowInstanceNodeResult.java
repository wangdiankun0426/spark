package com.spark.common.bean.flow.result;

import com.spark.common.bean.base.BaseResult;
import lombok.Data;

import java.util.List;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2025/11/2 19:19
 */
@Data
public class FlowInstanceNodeResult extends BaseResult {

    /**
     * 流程实例id
     */
    private Long instanceId;

    /**
     * 节点id
     */
    private String nodeId;

    /**
     * 节点名称
     */
    private String name;

    /**
     * 节点类型
     */
    private String type;

    /**
     * 审批人集合id
     */
    private String assigneeSetId;

    /**
     * 待审批人名称
     */
    private String unAssigneeName;

    /**
     * 节点状态 FlowInstanceStatusEnum
     */
    private Integer status;

    /**
     * 节点状态名称
     */
    private String statusName;

    /**
     * 讨论列表
     */
    private List<FlowInstanceDiscussResult> discusses;
}
