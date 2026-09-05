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
 * workFlow模板版本提交参数
 */
@Data
public class WfTemplateVersionVO extends BaseVO {

    /**
     * 工作流模板ID
     */
    private Long templateId;

    /**
     * DAG图定义（节点+边，JSON）
     */
    private String dagJson;

    /**
     * 全局变量定义（JSON）
     */
    private String globalVars;

}
