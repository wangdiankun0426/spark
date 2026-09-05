package com.spark.common.bean.dms.result;

import com.spark.common.bean.base.BaseResult;
import lombok.Data;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-07-18 10:00:00
 * 文档数量统计结果
 */
@Data
public class DocumentCountResult extends BaseResult {

    /**
     * 父ID
     */
    private Long prtId;

    /**
     * 文档数量
     */
    private Integer documentCount;

}
