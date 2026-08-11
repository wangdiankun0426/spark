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
 * AI工作流运行实例查询结果
 */
@Data
public class WfInstanceResult extends BaseResult {

    /**
     * 工作流模板ID
     */
    private Long templateId;

    /**
     * 工作流模板名称
     */
    private String templateName;

    /**
     * 执行版本ID
     */
    private Long revId;

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
     * 输入参数JSON
     */
    private String inputJson;

    /**
     * 输出结果JSON
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
     * 总耗时（毫秒）
     */
    private Long durationMs;

    /**
     * 触发方式
     */
    private Integer triggerType;

    /**
     * 触发方式名称
     */
    private String triggerTypeName;

}
