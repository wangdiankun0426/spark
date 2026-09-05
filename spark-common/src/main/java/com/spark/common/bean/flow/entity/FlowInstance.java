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
 * @since 2025-11-02 15:47:22
 */
@Data
public class FlowInstance extends BaseEntity {

    /**
     * 流程模板id
     **/
    private Long templateId;

    /**
     * 流程模板版本id
     **/
    private Long templateRevId;

    /**
     * 表单id
     **/
    private Long formId;

    /**
     * 表单版本id
     **/
    private Long formRevId;

    /**
     * 模板id
     **/
    private String processId;

    /**
     * flowbale流程实例id
     **/
    private String flowableInstanceId;

    /**
     * 状态 FlowInstanceStatusEnum
     **/
    private Integer status;

    /**
     * 紧急程度 FlowInstanceLevelEnum
     **/
    private Integer level;

    /**
     * 流程实例名称
     **/
    private String name;

    /**
     * 流程实例描述
     **/
    private String description;

}
