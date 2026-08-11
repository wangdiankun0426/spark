package com.spark.bean.workflow.query;

import com.spark.bean.base.BaseQuery;
import lombok.Data;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-08-11 10:00:00
 * AI工作流端点查询参数
 */
@Data
public class WfTemplateEndpointQuery extends BaseQuery {

    /**
     * 工作流模板ID
     */
    private Long templateId;

    /**
     * 端点路径
     */
    private String path;

}
