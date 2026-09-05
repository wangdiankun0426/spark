package com.spark.common.bean.sys.query;

import com.spark.common.bean.base.BaseQuery;
import lombok.Data;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-08-20 14:30:00
 * 系统附件查询参数
 */
@Data
public class AttachmentQuery extends BaseQuery {

    /**
     * 文件名称（模糊查询）
     */
    private String name;

    /**
     * 上传人ID
     */
    private Long ownerId;

    /**
     * 所属部门id
     */
    private Long deptId;
}
