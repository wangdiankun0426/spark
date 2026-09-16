package com.spark.common.bean.flow.vo;

import lombok.Data;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-09-16 10:24:00
 * 流程实例审批人替换参数
 */
@Data
public class FlowInstanceAssigneeReplaceVO {

    /**
     * 原审批人记录id，对应 flow_instance_assignee.id
     */
    private Long sourceId;

    /**
     * 新审批人用户id
     */
    private Long targetUserId;

}
