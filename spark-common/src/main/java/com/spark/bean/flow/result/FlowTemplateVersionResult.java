package com.spark.bean.flow.result;

import com.spark.bean.base.BaseResult;
import lombok.Data;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2025-11-01 13:34:43
 */
@Data
public class FlowTemplateVersionResult extends BaseResult {

    /**
     * 模板id
     **/
    private Long templateId;

    /**
     * 版本
     **/
    private Integer revNum;

    /**
     * 模板id
     **/
    private String processId;

    /**
     * bpm json文件保存路径
     **/
    private String bpmPath;

    /**
     * bpm json文件内容
     **/
    private String bpmJson;

    /**
     * 表单id
     **/
    private Long formId;
}
