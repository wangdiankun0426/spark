package com.spark.bean.workflow.vo;

import com.spark.bean.base.BaseVO;
import lombok.Data;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-08-11 10:00:00
 * AI工作流运行实例提交参数
 */
@Data
public class WfInstanceVO extends BaseVO {

    /**
     * 工作流模板ID
     */
    private Long templateId;

    /**
     * 输入参数JSON
     */
    private String inputJson;

    /**
     * 触发方式
     */
    private Integer triggerType;

}
