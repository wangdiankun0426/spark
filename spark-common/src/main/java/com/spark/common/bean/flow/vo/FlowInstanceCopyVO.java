package com.spark.common.bean.flow.vo;

import com.spark.common.bean.base.BaseVO;
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
public class FlowInstanceCopyVO extends BaseVO {

    /**
     * 流程实例id
     **/
    private Long instanceId;

    /**
     * 抄送来源节点id
     **/
    private Long nodeId;

    /**
     * 被抄送人id列表
     **/
    private List<Long> userIds;

}
