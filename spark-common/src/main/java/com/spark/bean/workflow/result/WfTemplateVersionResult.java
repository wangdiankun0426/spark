package com.spark.bean.workflow.result;

import com.spark.bean.base.BaseResult;
import lombok.Data;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-08-11 10:00:00
 * AI工作流模板版本查询结果
 */
@Data
public class WfTemplateVersionResult extends BaseResult {

    /**
     * 工作流模板ID
     */
    private Long templateId;

    /**
     * 版本序号（1开始递增）
     */
    private Integer revCode;

    /**
     * 版本号（0.1/0.2/...）
     */
    private String revNum;

    /**
     * DAG图定义（节点+边，JSON）
     */
    private String dagJson;

    /**
     * 全局变量定义（JSON）
     */
    private String globalVars;

}
