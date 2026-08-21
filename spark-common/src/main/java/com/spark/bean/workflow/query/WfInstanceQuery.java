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
 * AI工作流运行实例查询参数
 */
@Data
public class WfInstanceQuery extends BaseQuery {

    /**
     * 工作流模板ID
     */
    private Long templateId;

    /**
     * 运行状态：1运行中/2成功/3失败/4超时/5已取消
     */
    private Integer status;

}
