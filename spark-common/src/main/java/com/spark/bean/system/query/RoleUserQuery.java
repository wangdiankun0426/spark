package com.spark.bean.system.query;

import com.spark.bean.base.BaseQuery;
import lombok.Data;

import java.util.List;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/2/27 20:17
 */
@Data
public class RoleUserQuery extends BaseQuery {
    /**
     * 角色id
     */
    private Long roleId;

    /**
     * 用户ids
     */
    private List<Long> userIds;

    /**
     * 用户id
     */
    private Long userId;
}
