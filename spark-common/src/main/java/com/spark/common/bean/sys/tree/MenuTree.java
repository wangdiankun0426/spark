package com.spark.common.bean.sys.tree;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026/9/10 18:20
 * 菜单树，父子关系以标识码表达，故不继承 BaseTree
 */
@Data
public class MenuTree implements Serializable {

    /**
     * 顶级菜单的上级标识码
     */
    public final static String ROOT_PARENT_CODE = "";

    /**
     * 主键
     */
    private Long id;

    /**
     * 标识码
     */
    private String code;

    /**
     * 上级菜单标识码，顶级菜单为空
     */
    private String parentCode;

    /**
     * 菜单名称
     */
    private String name;

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

    /**
     * 子级
     */
    private List<MenuTree> children;
}
