package com.spark.common.bean.sys.result;

import com.spark.common.bean.base.BaseResult;
import lombok.Data;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-08-20 14:30:00
 * 系统附件结果
 */
@Data
public class AttachmentResult extends BaseResult {

    /**
     * 文件名称
     **/
    private String name;

    /**
     * 文件大小
     **/
    private Long size;

    /**
     * 文件大小字符串
     **/
    private String sizeStr;

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

    /**
     * 上传人名称
     **/
    private String ownerName;

    /**
     * 所属部门id
     **/
    private Long deptId;

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
