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
 * @since 2026-11-13 16:30:00
 * 菜单查询参数
 */
@Data
public class MenuQuery extends BaseQuery {

    /**
     * 菜单名称
     */
    private String name;

    /**
     * 标识码
     */
    private String code;

    /**
     * 上级菜单标识码
     */
    private String parentCode;

    /**
     * 类型 MenuTypeEnum
     */
    private Integer type;

    /**
     * 菜单状态 1正常0停用
     */
    private Integer status;

    /**
     * 平台归属 0租户1站点2组织
     */
    private Integer platform;
}