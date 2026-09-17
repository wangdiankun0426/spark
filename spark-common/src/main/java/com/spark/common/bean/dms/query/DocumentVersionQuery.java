package com.spark.common.bean.dms.query;

import com.spark.common.bean.base.BaseQuery;
import lombok.Data;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-09-17 10:00:00
 */
@Data
public class DocumentVersionQuery extends BaseQuery {

    /**
     * 文档id
     **/
    private Long docId;

    /**
     * 版本号
     **/
    private Integer versionNo;

    /**
     * 拓展名
     */
    private String ext;
}
