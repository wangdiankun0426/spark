package com.spark.bean.workflow.result;

import com.spark.bean.base.BaseResult;
import lombok.Data;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-08-11 10:00:00
 * AI工作流端点查询结果
 */
@Data
public class WfTemplateEndpointResult extends BaseResult {

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
