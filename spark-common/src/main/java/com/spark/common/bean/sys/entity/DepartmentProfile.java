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
 * @since 2026-09-08 16:00:00
 * 部门扩展信息表
 */
@Data
public class DepartmentProfile extends BaseEntity {

    /**
     * 企业微信部门ID
     */
    private Long wecomId;
}
