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
 * @since 2024-12-07 19:08:01
 */
@Data
public class DocumentQuery extends BaseQuery {

    /**
     * 父ID
     **/
    private Long prtId;

    /**
     * 文档归属类型
     **/
    private Integer documentType;

    /**
     * 名称
     */
    private String name;

    /**
     * 大小
     **/
    private Long size;

    /**
     * 存储路径
     */
    private String path;

    /**
     * 拓展名
     */
    private String ext;

    /**
     * 所有者ID
     **/
    private Long ownerId;

    /**
     * 分块索引
     **/
    private Integer chunkIndex;
}
