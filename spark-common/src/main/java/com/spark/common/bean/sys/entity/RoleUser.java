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
 * @since 2024/2/27 20:11
 */
@Data
public class RoleUser extends BaseEntity {
    /**
     * 角色id
     */
    private Long roleId;

    /**
     * 用户id
     */
    private Long  userId;
}
