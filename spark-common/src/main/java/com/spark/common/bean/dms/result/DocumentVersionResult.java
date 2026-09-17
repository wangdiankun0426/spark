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
 * @since 2026-09-17 10:00:00
 */
@Data
public class DocumentVersionResult extends BaseResult {

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

    /**
     * 文件大小字符串
     **/
    private String sizeStr;

    /**
     * 是否当前版本
     **/
    private Boolean currentFlag;

    public String getSizeStr() {
        if (size == null) {
            return "0MB";
        }
        if (size < 1024) {
            return size + "B";
        }
        if (size < 1024 * 1024) {
            return size / 1024 + "KB";
        }
        return size / 1024 / 1024 + "MB";
    }
}
