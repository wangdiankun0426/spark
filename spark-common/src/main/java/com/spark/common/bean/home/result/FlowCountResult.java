package com.spark.common.bean.home.result;

import lombok.Data;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-10-29 20:00:00
 * 我的流程四项数量
 */
@Data
public class FlowCountResult {

    /**
     * 待办数量
     */
    private Integer todoCount;

    /**
     * 已办数量
     */
    private Integer doneCount;

    /**
     * 我的申请数量
     */
    private Integer applicationCount;

    /**
     * 抄送我的数量
     */
    private Integer copyCount;
}
