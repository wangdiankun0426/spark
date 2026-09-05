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

}
