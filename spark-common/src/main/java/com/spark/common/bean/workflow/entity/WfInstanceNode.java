package com.spark.common.bean.workflow.entity;

import com.spark.common.bean.base.BaseEntity;
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
 * workFlow实例节点运行记录表实体
 */
@Data
public class WfInstanceNode extends BaseEntity {

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
     * 节点类型（llm/knowledge/code/...）
     */
    private String nodeType;

    /**
     * 状态：1等待/2运行中/3成功/4失败/5跳过
     */
    private Integer status;

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
