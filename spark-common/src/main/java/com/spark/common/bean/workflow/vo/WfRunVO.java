package com.spark.common.bean.workflow.vo;

import com.spark.common.bean.base.BaseVO;
import com.spark.common.bean.form.entity.FormObjValue;
import lombok.Data;

import java.util.List;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-08-22 10:00:00
 * 工作流运行入参
 */
@Data
public class WfRunVO extends BaseVO {

    /**
     * 工作流模板ID
     */
    private Long templateId;

    /**
     * 绑定输入表单ID
     */
    private Long formId;

    /**
     * 表单值
     */
    private List<FormObjValue> values;
}
