package com.spark.bean.system.entity;

import com.spark.bean.base.BaseEntity;
import lombok.Data;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/2/17 18:48
 */
@Data
public class Department extends BaseEntity {

    /**
     * 父级id
     */
    private Long prtId;

    /**
     * 名称
     */
    private String name;

    /**
     * 层级码
     */
    private String code;

    /**
     * 部门领导id
     */
    private Long headerId;

    /**
     * 部门编号
     */
    private String deptNum;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 排序号
     */
    private Integer orderNum;

    /**
     * 企业微信部门ID
     */
    private Long wecomId;
}
