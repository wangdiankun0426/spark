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
 * @since 2024-12-07 19:08:01
 */
@Data
public class DocumentResult extends BaseResult {

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
     * 当前版本号
     **/
    private Integer versionNo;

    /**
     * 文件大小字符串
     **/
    private String sizeStr;

    /**
     * 所有者ID
     **/
    private Long ownerId;

    /**
     * 所有者名称
     **/
    private String ownerName;

    /**
     * 父名称
     **/
    private String prtName;

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
