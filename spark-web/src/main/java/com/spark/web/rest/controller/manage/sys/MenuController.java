package com.spark.web.rest.controller.manage.sys;

import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.sys.entity.Menu;
import com.spark.common.bean.sys.query.MenuQuery;
import com.spark.common.bean.sys.tree.MenuTree;
import com.spark.common.bean.sys.vo.MenuVO;
import com.spark.manage.sys.IMenuService;
import com.spark.config.aspectj.annotation.RequiresPermi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
@RestController
@RequestMapping("sys/menu")
public class MenuController {
    @Autowired
    private IMenuService menuService;

    /**
     * 查询菜单列表
     * @return 查询结果
     */
    @GetMapping("list")
    public ResultData<List<Menu>> queryMenuList() {
        return menuService.queryMenuList();
    }

    /**
     * 查询菜单管理树
     * @return 查询结果
     */
    @GetMapping("tree")
    public ResultData<List<MenuTree>> queryMenuTreeList() {
        return menuService.queryMenuTreeList();
    }

    /**
     * 查询菜单详情
     * @param query 查询参数
     * @return 菜单
     */
    @GetMapping("detail")
    public ResultData<Menu> queryMenuDetail(MenuQuery query) {
        return menuService.queryMenuDetail(query);
    }

    /**
     * 创建菜单
     * @param menuVO 菜单参数
     * @return 创建结果
     */
    @PostMapping("create")
    public ResultData<Void> createMenu(MenuVO menuVO) {
        return menuService.createMenu(menuVO);
    }

    /**
     * 修改菜单
     * @param menuVO 菜单参数
     * @return 修改结果
     */
    @PostMapping("update")
    public ResultData<Void> updateMenu(MenuVO menuVO) {
        return menuService.updateMenu(menuVO);
    }

    /**
     * 删除菜单
     * @param menuVO 删除参数
     * @return 删除结果
     */
    @PostMapping("delete")
    public ResultData<Void> deleteMenu(MenuVO menuVO) {
        return menuService.deleteMenu(menuVO);
    }

    /**
     * 查询角色授权菜单树
     * @return 菜单树
     */
    @RequiresPermi("sys:role:query")
    @GetMapping("roleTree")
    public ResultData<List<MenuTree>> queryRoleMenuTreeList() {
        return menuService.queryRoleMenuTreeList();
    }

    /**
     * 查询租户菜单授权树
     * @return 菜单树
     */
    @GetMapping("tenantTree")
    public ResultData<List<MenuTree>> queryTenantMenuTreeList() {
        return menuService.queryTenantMenuTreeList();
    }
}
