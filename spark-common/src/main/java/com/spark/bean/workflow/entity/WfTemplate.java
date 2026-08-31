package com.spark.bean.workflow.entity;

import com.spark.bean.base.BaseEntity;
import lombok.Data;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-08-11 10:00:00
 * workFlow模板表实体
 */
@Data
public class WfTemplate extends BaseEntity {

    /**
     * 工作流名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * 状态：0关闭/1开启
     */
    private Integer status;

    /**
     * 当前已发布版本ID
     */
    private Long revId;

    /**
     * 当前已发布版本号
     */
    private String revNum;

    /**
     * 绑定输入表单ID
     */
    private Long formId;

}
