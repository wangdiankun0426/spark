package com.spark.common.bean.workflow.query;

import com.spark.common.bean.base.BaseQuery;
import lombok.Data;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-08-11 10:00:00
 * workFlow运行实例查询参数
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
