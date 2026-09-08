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
 * @since 2026-09-08 10:00:00
 * 租户
 */
@Data
public class TenantQuery extends BaseQuery {

    /**
     * 租户名，模糊查询
     */
    private String name;

    /**
     * 状态 StatusEnum
     */
    private Integer status;
}
