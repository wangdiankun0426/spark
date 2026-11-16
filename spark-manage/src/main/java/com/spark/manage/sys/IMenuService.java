package com.spark.manage.sys;

import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.sys.entity.Menu;
import com.spark.common.bean.sys.query.MenuQuery;
import com.spark.common.bean.sys.tree.MenuTree;
import com.spark.common.bean.sys.vo.MenuVO;

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
    ResultData<List<Menu>> queryMenuList();

    /**
     * 查询菜单管理树，排除工作台与站点管理菜单
     * @return 查询结果
     */
    ResultData<List<MenuTree>> queryMenuTreeList();

    /**
     * 查询菜单详情
     * @param query 查询参数
     * @return 菜单
     */
    ResultData<Menu> queryMenuDetail(MenuQuery query);

    /**
     * 创建菜单
     * @param menuVO 菜单参数
     * @return 创建结果
     */
    ResultData<Void> createMenu(MenuVO menuVO);

    /**
     * 修改菜单
     * @param menuVO 菜单参数
     * @return 修改结果
     */
    ResultData<Void> updateMenu(MenuVO menuVO);

    /**
     * 删除菜单
     * @param menuVO 删除参数
     * @return 删除结果
     */
    ResultData<Void> deleteMenu(MenuVO menuVO);

    /**
     * 查询角色授权菜单树，仅租户业务平台菜单，非站点管理员限当前租户已授权菜单
     * @return 菜单树
     */
    ResultData<List<MenuTree>> queryRoleMenuTreeList();

    /**
     * 查询租户菜单授权树，含租户与组织平台菜单与按钮，供站点管理员按租户勾选
     * @return 菜单树
     */
    ResultData<List<MenuTree>> queryTenantMenuTreeList();
}