package com.spark.common.bean.task.result;

import com.spark.common.bean.base.BaseResult;
import lombok.Data;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-08-20 10:00:00
 * 任务模板参数表
 */
@Data
public class TaskTemplateParamResult extends BaseResult {

    /**
     * 模板id
     */
    private Long templateId;

    /**
     * 参数名称
     */
    private String name;

    /**
     * 参数编码
     */
    private String code;

    /**
     * 参数类型 TaskParamTypeEnum
     */
    private Integer type;

    /**
     * 参数类型名称
     */
    private String typeName;
}
