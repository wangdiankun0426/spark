package com.spark.common.bean.workflow.result;

import com.spark.common.bean.base.BaseResult;
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
 * workFlow运行实例查询结果
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
     * 错误信息
     */
    private String errorMsg;

    /**
     * 结束时间
     */
    private Timestamp finishedDt;

    /**
     * 总耗时（毫秒）
     */
    private Long durationMs;

}
