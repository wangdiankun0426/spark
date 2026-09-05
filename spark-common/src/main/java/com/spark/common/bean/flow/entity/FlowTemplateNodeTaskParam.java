package com.spark.common.bean.flow.entity;

import com.spark.common.bean.base.BaseEntity;
import lombok.Data;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-08-20 15:00:00
 * 流程模板节点任务参数
 */
@Data
public class FlowTemplateNodeTaskParam extends BaseEntity {

    /**
     * 节点任务id
     */
    private Long nodeTaskId;

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
     * 参数值（表单数据为#{form:code}#/#{formTxt:code}#表达式）
     */
    private String value;

}
