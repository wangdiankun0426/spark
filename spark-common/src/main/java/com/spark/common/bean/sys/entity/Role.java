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
 * @since 2024/2/25 20:13
 */
@Data
public class Role extends BaseEntity {

    /**
     * 名称
     */
    private String name;
    /**
     * 数据权限 DataScopeEnum
     */
    private Integer dataScope;
    /**
     * 状态 StatusEnum
     */
    private Integer status;
}
