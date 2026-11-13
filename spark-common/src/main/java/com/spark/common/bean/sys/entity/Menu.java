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

    /**
     * 权限串 模块:功能:操作
     */
    private String perms;

    /**
     * 路由地址
     */
    private String path;

    /**
     * 组件路径 相对src无后缀
     */
    private String component;

    /**
     * 图标名
     */
    private String icon;

    /**
     * 排序号
     */
    private Integer orderNum;

    /**
     * 显示状态 1显示0隐藏
     */
    private Integer visible;

    /**
     * 菜单状态 1正常0停用
     */
    private Integer status;

    /**
     * 平台归属 MenuPlatformEnum
     */
    private Integer platform;
}
