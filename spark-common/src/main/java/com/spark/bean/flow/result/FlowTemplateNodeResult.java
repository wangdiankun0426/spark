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
 * @since 2025/11/2 18:30
 */
@Data
public class FlowTemplateNodeResult extends BaseResult {

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
     * 节点名称
     */
    private String name;

    /**
     * 节点类型
     */
    private String type;

    /**
     * 节点审批人类型
     */
    private Integer assigneeType;

    /**
     * 审批人
     */
    private String assignee;

    /**
     * 节点权限
     */
    private Integer permission;
}
