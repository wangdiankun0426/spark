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
 * AI工作流端点配置参数
 */
@Data
public class WfTemplateEndpointVO extends BaseVO {

    /**
     * 工作流模板ID
     */
    private Long templateId;

    /**
     * 生效版本ID
     */
    private Long revId;

    /**
     * 端点路径
     */
    private String path;

    /**
     * 鉴权方式：0无/1登录态/2API Key
     */
    private Integer authType;

    /**
     * API Key（authType=2时使用）
     */
    private String apiKey;

    /**
     * 是否启用
     */
    private Integer enabled;

}
