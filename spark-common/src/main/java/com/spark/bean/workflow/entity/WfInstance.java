package com.spark.bean.workflow.entity;

import com.spark.bean.base.BaseEntity;
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
 * AI工作流运行实例表实体
 */
@Data
public class WfInstance extends BaseEntity {

    /**
     * 工作流模板ID
     */
    private Long templateId;

    /**
     * 执行版本ID
     */
    private Long revId;

    /**
     * 执行版本号
     */
    private String revNum;

    /**
     * 运行状态：1运行中/2成功/3失败/4超时/5已取消
     */
    private Integer status;

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
     * 触发方式：1手动/2API/3定时
     */
    private Integer triggerType;

}
