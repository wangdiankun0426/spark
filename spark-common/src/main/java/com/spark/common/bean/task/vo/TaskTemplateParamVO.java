package com.spark.common.bean.task.vo;

import com.spark.common.bean.base.BaseVO;
import lombok.Data;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-08-20 10:00:00
 * 任务模板参数参数
 */
@Data
public class TaskTemplateParamVO extends BaseVO {

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
}
