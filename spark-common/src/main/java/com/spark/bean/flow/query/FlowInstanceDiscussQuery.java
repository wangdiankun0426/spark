package com.spark.bean.flow.query;

import com.spark.bean.base.BaseQuery;
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
public class FlowInstanceDiscussQuery extends BaseQuery {

    /**
     * 流程实例ID
     */
    private Long instanceId;

    /**
     * 流程节点ID
     */
    private Long instanceNodeId;

    /**
     * 审批人id
     */
    private Long assigneeId;
}
