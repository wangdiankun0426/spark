package com.spark.manage.sys;

import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.sys.entity.Menu;
import com.spark.common.bean.sys.tree.MenuTree;

import java.util.List;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-09-06 11:40:00
 */
public interface IMenuService {

    /**
     * 查询菜单列表
     * @return 查询结果
     */
    ResultData<List<Menu>> listMenu();

    /**
     * 查询菜单树
     * @return 查询结果
     */
    ResultData<List<MenuTree>> listMenuTree();
}
