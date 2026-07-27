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
 * @since 2025/11/2 18:25
 */
@Data
public class FlowInstanceNode extends BaseEntity {

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
     * 节点状态 FlowInstanceStatusEnum
     */
    private Integer status;
}
