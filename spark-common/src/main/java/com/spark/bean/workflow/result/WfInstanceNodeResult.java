package com.spark.bean.workflow.result;

import com.spark.bean.base.BaseResult;
import lombok.Data;

import java.sql.Timestamp;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-08-11 10:00:00
 * workFlow实例节点运行记录查询结果
 */
@Data
public class WfInstanceNodeResult extends BaseResult {

    /**
     * 运行实例ID
     */
    private Long instanceId;

    /**
     * DAG节点ID
     */
    private String nodeId;

    /**
     * 节点名称
     */
    private String nodeName;

    /**
     * 节点类型 WorkflowTemplateTypeEnum
     */
    private String nodeType;

    /**
     * 状态 WorkflowNodeStatusEnum
     */
    private Integer status;

    /**
     * 状态名称
     */
    private String statusName;

    /**
     * 节点输入JSON快照
     */
    private String inputJson;

    /**
     * 节点输出JSON快照
     */
    private String outputJson;

    /**
     * 错误信息
     */
    private String errorMsg;

    /**
     * 开始时间
     */
    private Timestamp startedDt;

    /**
     * 结束时间
     */
    private Timestamp finishedDt;

    /**
     * 耗时（毫秒）
     */
    private Long durationMs;

}
