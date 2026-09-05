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
 * @since 2025-10-29 20:57:44
 */
@Data
public class FlowTemplate extends BaseEntity {

    /**
     * 名称
     **/
    private String name;

    /**
     * 模板id
     **/
    private String processId;

    /**
     * 版本id
     **/
    private Long revId;

    /**
     * 版本
     **/
    private String revNum;

    /**
     * 状态
     **/
    private Integer status;

    /**
     * 表单id
     **/
    private Long formId;

    /**
     * 流程类型
     **/
    private Integer type;

    /**
     * 备注
     **/
    private String remark;

}
