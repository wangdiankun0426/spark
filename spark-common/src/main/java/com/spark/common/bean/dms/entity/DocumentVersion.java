package com.spark.common.bean.dms.entity;

import com.spark.common.bean.base.BaseEntity;
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
public class DocumentVersion extends BaseEntity {

    /**
     * 文档id
     **/
    private Long docId;

    /**
     * 版本号
     **/
    private Integer versionNo;

    /**
     * 名称
     **/
    private String name;

    /**
     * 大小
     **/
    private Long size;

    /**
     * 存储路径
     **/
    private String path;

    /**
     * 拓展名
     **/
    private String ext;
}
