package com.spark.common.bean.sys.result;

import com.spark.common.bean.base.BaseResult;
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
public class TenantConfigResult extends BaseResult {

    /**
     * 配置名称
     */
    private String name;

    /**
     * 配置key
     */
    private String key;

    /**
     * 配置值
     */
    private String value;
}
