package com.spark.common.bean.sys.query;

import com.spark.common.bean.base.BaseQuery;
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
     * 角色ids
     */
    private List<Long> roleIds;

    /**
     * 用户ids
     */
    private List<Long> userIds;

    /**
     * 用户id
     */
    private Long userId;
}
