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
 * @since 2026-09-11 10:00:00
 * 租户配置
 */
@Data
public class TenantConfigQuery extends BaseQuery {

    /**
     * 配置名称，模糊查询
     */
    private String name;

    /**
     * 配置key
     */
    private String key;
}
