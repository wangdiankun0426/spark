package com.spark.common.bean.home.result;

import lombok.Data;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-11-05 15:00:00
 * 工作台总览，合并模型用量与资产数量
 */
@Data
public class DashboardOverviewResult {

    /**
     * 模型调用次数
     */
    private Integer callCount;

    /**
     * 输入token
     */
    private Long inputTokens;

    /**
     * 输出token
     */
    private Long outputTokens;

    /**
     * 总token
     */
    private Long totalTokens;

    /**
     * 模型数量
     */
    private Integer modelCount;

    /**
     * 智能体数量
     */
    private Integer agentCount;

    /**
     * 工作流数量
     */
    private Integer workflowCount;
}
