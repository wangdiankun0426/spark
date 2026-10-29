package com.spark.common.bean.home.result;

import lombok.Data;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-10-29 18:00:00
 * 我的资产数量
 */
@Data
public class DashboardAssetResult {

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
