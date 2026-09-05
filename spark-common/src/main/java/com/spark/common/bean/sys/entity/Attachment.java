package com.spark.common.bean.sys.entity;

import com.spark.common.bean.base.BaseEntity;
import lombok.Data;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-08-20 14:30:00
 * 系统附件实体
 */
@Data
public class Attachment extends BaseEntity {

    /**
     * 文件名称
     **/
    private String name;

    /**
     * 文件大小
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

    /**
     * 上传人ID
     **/
    private Long ownerId;
}
