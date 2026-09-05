package com.spark.common.bean.workflow.result;

import com.spark.common.bean.base.BaseResult;
import lombok.Data;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-08-11 10:00:00
 * workFlow模板查询结果
 */
@Data
public class WfTemplateResult extends BaseResult {

    /**
     * 工作流名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * 状态：-1关闭/1开启
     */
    private Integer status;

    /**
     * 状态名称
     */
    private String statusName;

    /**
     * 当前生效版本ID
     */
    private Long revId;

    /**
     * 当前生效版本号
     */
    private String revNum;

    /**
     * 绑定输入表单ID
     */
    private Long formId;

    /**
     * 绑定表单版本ID
     */
    private Long formRevId;

    /**
     * 绑定表单JSON定义
     */
    private String formJson;

}
