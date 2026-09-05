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
 * @since 2025/10/19 15:17
 */
@Data
public class DocumentChunkResult extends BaseResult {

    private Long docId;

    private String content;
}
