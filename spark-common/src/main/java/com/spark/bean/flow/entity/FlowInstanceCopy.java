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
 * @since 2026-08-27 10:00:00
 */
@Data
public class FlowInstanceCopy extends BaseEntity {

    /**
     * 流程实例id
     **/
    private Long instanceId;

    /**
     * 被抄送人id
     **/
    private Long userId;

    /**
     * 抄送来源节点id
     **/
    private String nodeId;

}
