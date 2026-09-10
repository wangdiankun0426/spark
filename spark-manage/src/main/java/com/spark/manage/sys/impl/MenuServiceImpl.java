package com.spark.manage.sys.impl;

import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.sys.entity.Menu;
import com.spark.common.bean.sys.tree.MenuTree;
import com.spark.common.utils.BeanUtil;
import com.spark.common.utils.CollectionUtil;
import com.spark.common.utils.StringUtil;
import com.spark.config.aspectj.annotation.LogPrint;
import com.spark.dao.sys.MenuDao;
import com.spark.manage.sys.IMenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-09-06 11:40:00
 */
@Service
@LogPrint
public class MenuServiceImpl implements IMenuService {
    @Autowired
    private MenuDao menuDao;

    /**
     * 查询菜单列表
     * @return 查询结果
     */
    @Override
    public ResultData<List<Menu>> listMenu() {
        ResultData<List<Menu>> result = new ResultData<>();
        List<Menu> menuList = menuDao.queryMenuList();
        result.setData(menuList);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 查询菜单树
     * @return 查询结果
     */
    @Override
    public ResultData<List<MenuTree>> listMenuTree() {
        ResultData<List<MenuTree>> result = new ResultData<>();
        List<Menu> menuList = menuDao.queryMenuList();
        result.setData(this.buildMenuTree(menuList));
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 按上级菜单标识码构建菜单树
     * @param menuList 菜单列表
     * @return 菜单树
     */
    private List<MenuTree> buildMenuTree(List<Menu> menuList) {
        List<MenuTree> treeList = new ArrayList<>();
        if (CollectionUtil.isEmpty(menuList)) {
            return treeList;
        }
        // 按上级标识码分组，LinkedHashMap 保持菜单原有顺序
        Map<String, List<MenuTree>> childrenMap = new LinkedHashMap<>();
        for (Menu menu : menuList) {
            MenuTree menuTree = new MenuTree();
            BeanUtil.copyProperties(menu, menuTree);
            String parentCode = StringUtil.isBlank(menu.getParentCode())
                    ? MenuTree.ROOT_PARENT_CODE : menu.getParentCode();
            childrenMap.computeIfAbsent(parentCode, k -> new ArrayList<>()).add(menuTree);
        }
        // 回填各节点的子级
        childrenMap.forEach((parentCode, children) -> children.forEach(node ->
                node.setChildren(childrenMap.getOrDefault(node.getCode(), new ArrayList<>()))));
        treeList.addAll(childrenMap.getOrDefault(MenuTree.ROOT_PARENT_CODE, new ArrayList<>()));
        return treeList;
    }
}
