package com.spark.bean.flow.entity;

import com.spark.bean.base.BaseEntity;
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
public class FlowTemplateVersion extends BaseEntity {

    /**
     * 模板id
     **/
    private Long templateId;

    /**
     * 版本编码
     **/
    private Integer revCode;

    /**
     * 版本
     **/
    private String revNum;

    /**
     * 模板id
     **/
    private String processId;

    /**
     * bpm json文件保存路径
     **/
    private String bpmPath;

}
