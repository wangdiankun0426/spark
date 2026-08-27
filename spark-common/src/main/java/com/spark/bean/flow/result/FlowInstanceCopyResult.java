package com.spark.bean.flow.result;

import com.spark.bean.base.BaseResult;
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
public class FlowInstanceCopyResult extends BaseResult {

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

    /**
     * 流程实例名称
     **/
    private String name;

    /**
     * 流程实例状态
     **/
    private Integer status;

    /**
     * 状态名称
     **/
    private String statusName;

    /**
     * 紧急程度
     **/
    private Integer level;

    /**
     * 紧急程度名称
     **/
    private String levelName;

    /**
     * 申请部门id
     **/
    private Long deptId;

    /**
     * 申请部门名称
     **/
    private String deptName;

    /**
     * 申请人名称
     **/
    private String appByName;

}
