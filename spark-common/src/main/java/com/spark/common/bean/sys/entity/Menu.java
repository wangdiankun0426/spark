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
 * @since 2026-09-06 11:40:00
 */
@Data
public class Menu extends BaseEntity {

    /**
     * 菜单名称
     */
    private String name;

    /**
     * 上级菜单标识码，顶级菜单为空
     */
    private String parentCode;

    /**
     * 标识码
     */
    private String code;

    /**
     * 类型 MenuTypeEnum
     */
    private Integer type;
}
