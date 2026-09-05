package com.spark.common.bean.workflow.vo;

import com.spark.common.bean.base.BaseVO;
import lombok.Data;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-08-11 10:00:00
 */
@Data
public class WfTemplateVO extends BaseVO {

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
     * 绑定输入表单ID
     */
    private Long formId;

}
