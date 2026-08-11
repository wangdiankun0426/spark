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
 * AI工作流模板版本表实体
 */
@Data
public class WfTemplateVersion extends BaseEntity {

    /**
     * 工作流模板ID
     */
    private Long templateId;

    /**
     * 版本序号
     */
    private Integer revCode;

    /**
     * 版本号
     */
    private String revNum;

    /**
     * DAG图定义（节点+边，JSON）
     */
    private String dagJson;

    /**
     * 全局变量定义（JSON）
     */
    private String globalVars;

}
