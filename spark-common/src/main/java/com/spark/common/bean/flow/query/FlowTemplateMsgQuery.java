package com.spark.common.bean.flow.query;

import com.spark.common.bean.base.BaseQuery;
import lombok.Data;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-08-06 10:00:00
 * 流程模板消息通知查询
 */
@Data
public class FlowTemplateMsgQuery extends BaseQuery {

    /**
     * 流程模板id
     */
    private Long templateId;

    /**
     * 流程版本id
     */
    private Long revId;

    /**
     * 通知类型
     */
    private Integer type;

}