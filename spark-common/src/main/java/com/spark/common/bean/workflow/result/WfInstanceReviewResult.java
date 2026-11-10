package com.spark.common.bean.workflow.result;

import com.spark.common.bean.base.BaseResult;
import lombok.Data;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-11-10 10:00:00
 * 工作流待我审核查询结果
 */
@Data
public class WfInstanceReviewResult extends BaseResult {

    /**
     * 运行实例ID
     */
    private Long instanceId;

    /**
     * 工作流模板ID
     */
    private Long templateId;

    /**
     * 工作流模板名称
     */
    private String templateName;

    /**
     * 执行版本号
     */
    private String revNum;

    /**
     * 运行状态
     */
    private Integer status;

    /**
     * 状态名称
     */
    private String statusName;

    /**
     * 待审核节点的DAG节点ID
     */
    private String nodeId;

    /**
     * 待审核节点运行记录行ID
     */
    private Long nodeRunId;

    /**
     * 待审核节点名称
     */
    private String nodeName;

    /**
     * 审核提示信息
     */
    private String prompt;

    /**
     * 审核人ID列表，逗号分隔
     */
    private String reviewerIds;
}
