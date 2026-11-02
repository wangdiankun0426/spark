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
 * @since 2026-08-27 10:00:00
 */
@Data
public class FlowInstanceCopyQuery extends BaseQuery {

    /**
     * 流程实例id
     */
    private Long instanceId;

    /**
     * 被抄送人id
     */
    private Long userId;

    /**
     * 流程名称，模糊匹配所属流程实例
     */
    private String name;

    /**
     * 流程状态集合，过滤所属流程实例的状态
     */
    private List<Integer> statusList;

    /**
     * 紧急程度
     */
    private Integer level;

}
