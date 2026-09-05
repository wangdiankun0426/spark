package com.spark.common.bean.flow.query;

import com.spark.common.bean.base.BaseQuery;
import lombok.Data;

import java.util.List;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-08-20 15:00:00
 * 流程模板节点任务参数查询
 */
@Data
public class FlowTemplateNodeTaskParamQuery extends BaseQuery {

    /**
     * 节点任务id
     */
    private Long nodeTaskId;

    /**
     * 节点任务id列表
     */
    private List<Long> nodeTaskIds;

}
