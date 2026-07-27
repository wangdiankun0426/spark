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
