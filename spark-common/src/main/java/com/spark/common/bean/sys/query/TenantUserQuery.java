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
 * @since 2026-09-08 14:00:00
 * 租户用户
 */
@Data
public class TenantUserQuery extends BaseQuery {

    /**
     * 租户id
     */
    private Long tenantId;

    /**
     * 用户ids
     */
    private List<Long> userIds;

    /**
     * 用户id
     */
    private Long userId;
}
