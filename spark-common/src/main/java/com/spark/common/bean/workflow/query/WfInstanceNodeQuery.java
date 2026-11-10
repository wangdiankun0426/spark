package com.spark.common.bean.workflow.query;

import com.spark.common.bean.base.BaseQuery;
import lombok.Data;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-08-11 10:00:00
 * workFlow实例节点运行记录查询参数
 */
@Data
public class WfInstanceNodeQuery extends BaseQuery {

    /**
     * 运行实例ID
     */
    private Long instanceId;

    /**
     * 节点类型 WorkflowTemplateTypeEnum
     */
    private String nodeType;

    /**
     * 节点状态 WorkflowNodeStatusEnum
     */
    private Integer status;

    /**
     * 所属实例运行状态，待审核候选查询联表过滤
     */
    private Integer instanceStatus;

}
