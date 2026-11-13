package com.spark.common.bean.sys.vo;

import lombok.Data;

import java.util.List;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-11-13 15:00:00
 * 前端动态路由节点，仅承载目录与菜单，按钮权限不进路由
 */
@Data
public class RouterVO {

    /**
     * 路由名称，取菜单标识码
     */
    private String name;

    /**
     * 路由地址
     */
    private String path;

    /**
     * 组件路径 相对src无后缀
     */
    private String component;

    /**
     * 是否隐藏
     */
    private Boolean hidden;

    /**
     * 重定向地址，由前端按首个可达子菜单计算
     */
    private String redirect;

    /**
     * 菜单标题
     */
    private String title;

    /**
     * 图标名
     */
    private String icon;

    /**
     * 菜单标识码
     */
    private String code;

    /**
     * 排序号
     */
    private Integer orderNum;

    /**
     * 子级路由
     */
    private List<RouterVO> children;
}