package com.spark.manage.sys.impl;

import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.base.SessionHolder;
import com.spark.common.bean.sys.entity.Menu;
import com.spark.common.bean.sys.query.MenuQuery;
import com.spark.common.bean.sys.tree.MenuTree;
import com.spark.common.bean.sys.vo.MenuVO;
import com.spark.common.enums.ErrorCodeEnum;
import com.spark.common.enums.MenuPlatformEnum;
import com.spark.common.enums.MenuTypeEnum;
import com.spark.common.enums.StatusEnum;
import com.spark.common.utils.BeanUtil;
import com.spark.common.utils.CollectionUtil;
import com.spark.common.utils.StringUtil;
import com.spark.config.aspectj.annotation.LogPrint;
import com.spark.dao.sys.MenuDao;
import com.spark.dao.sys.RoleMenuDao;
import com.spark.dao.sys.TenantMenuDao;
import com.spark.manage.sys.IMenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    /**
     * 租户菜单平台归属
     */
    private static final int PLATFORM_TENANT = MenuPlatformEnum.TENANT.getValue();

    /**
     * 组织菜单平台归属
     */
    private static final int PLATFORM_ORG = MenuPlatformEnum.ORG.getValue();

    /**
     * 祖先链防环最大遍历深度
     */
    private static final int MAX_MENU_DEPTH = 20;

    @Autowired
    private MenuDao menuDao;
    @Autowired
    private RoleMenuDao roleMenuDao;
    @Autowired
    private TenantMenuDao tenantMenuDao;

    /**
     * 查询菜单列表
     * @return 查询结果
     */
    @Override
    public ResultData<List<Menu>> queryMenuList() {
        ResultData<List<Menu>> result = new ResultData<>();
        if (!SessionHolder.isSysAdmin()) {
            result.setErrorCode(ErrorCodeEnum.NO_PERMISSION);
            return result;
        }
        List<Menu> menuList = menuDao.queryMenuList();
        result.setData(menuList);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 查询菜单管理树，含全部平台菜单
     * @return 查询结果
     */
    @Override
    public ResultData<List<MenuTree>> queryMenuTreeList() {
        ResultData<List<MenuTree>> result = new ResultData<>();
        if (!SessionHolder.isSysAdmin()) {
            result.setErrorCode(ErrorCodeEnum.NO_PERMISSION);
            return result;
        }
        List<Menu> menuList = menuDao.queryMenuList();
        List<MenuTree> treeList = this.buildMenuTree(menuList);
        result.setData(treeList);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 查询菜单详情
     * @param query 查询参数
     * @return 菜单
     */
    @Override
    public ResultData<Menu> queryMenuDetail(MenuQuery query) {
        ResultData<Menu> result = new ResultData<>();
        if (!SessionHolder.isSysAdmin()) {
            result.setErrorCode(ErrorCodeEnum.NO_PERMISSION);
            return result;
        }
        if (query == null || query.getId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        Menu menu = menuDao.queryMenu(query);
        if (menu == null) {
            result.setErrorCode(ErrorCodeEnum.MENU_NOT_EXIST);
            return result;
        }
        result.setData(menu);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 创建菜单
     * @param menuVO 菜单参数
     * @return 创建结果
     */
    @Override
    public ResultData<Void> createMenu(MenuVO menuVO) {
        ResultData<Void> result = new ResultData<>();
        if (!SessionHolder.isSysAdmin()) {
            result.setErrorCode(ErrorCodeEnum.NO_PERMISSION);
            return result;
        }
        ResultData<Void> validateResult = this.validateMenu(menuVO);
        if (validateResult.getCode() != ResultData.OK) {
            return validateResult;
        }
        Menu menu = new Menu();
        BeanUtil.copyProperties(menuVO, menu);
        this.applyMenuDefaults(menu);
        int count = menuDao.insertDB(menu);
        if (count < 1) {
            result.setErrorCode(ErrorCodeEnum.INSERT_DATA_FAIL);
            return result;
        }
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 修改菜单
     * @param menuVO 菜单参数
     * @return 修改结果
     */
    @Override
    public ResultData<Void> updateMenu(MenuVO menuVO) {
        ResultData<Void> result = new ResultData<>();
        if (!SessionHolder.isSysAdmin()) {
            result.setErrorCode(ErrorCodeEnum.NO_PERMISSION);
            return result;
        }
        if (menuVO == null || menuVO.getId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        ResultData<Void> validateResult = this.validateMenu(menuVO);
        if (validateResult.getCode() != ResultData.OK) {
            return validateResult;
        }
        Menu menu = new Menu();
        BeanUtil.copyProperties(menuVO, menu);
        this.applyMenuDefaults(menu);
        int count = menuDao.updateDBById(menu);
        if (count < 1) {
            result.setErrorCode(ErrorCodeEnum.UPDATE_DATA_FAIL);
            return result;
        }
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 删除菜单
     * @param menuVO 删除参数
     * @return 删除结果
     */
    @Override
    public ResultData<Void> deleteMenu(MenuVO menuVO) {
        ResultData<Void> result = new ResultData<>();
        if (!SessionHolder.isSysAdmin()) {
            result.setErrorCode(ErrorCodeEnum.NO_PERMISSION);
            return result;
        }
        if (menuVO == null || menuVO.getId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        Long menuId = menuVO.getId();
        MenuQuery menuQuery = new MenuQuery();
        menuQuery.setId(menuId);
        Menu menu = menuDao.queryMenu(menuQuery);
        if (menu == null) {
            result.setErrorCode(ErrorCodeEnum.MENU_NOT_EXIST);
            return result;
        }
        int childCount = menuDao.queryMenuCountByParentCode(menu.getCode());
        if (childCount > 0) {
            result.setErrorCode(ErrorCodeEnum.MENU_CHILD_EXIST);
            return result;
        }
        int roleCount = roleMenuDao.countByMenuId(menuId);
        if (roleCount > 0) {
            result.setErrorCode(ErrorCodeEnum.MENU_ROLE_ASSIGNED);
            return result;
        }
        int tenantGrantCount = tenantMenuDao.countByMenuId(menuId);
        if (tenantGrantCount > 0) {
            result.setErrorCode(ErrorCodeEnum.MENU_TENANT_ASSIGNED);
            return result;
        }
        int count = menuDao.deleteMenuById(menuId);
        if (count < 1) {
            result.setErrorCode(ErrorCodeEnum.DELETE_DATA_FAIL);
            return result;
        }
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 查询角色授权菜单树，仅租户菜单平台菜单，非站点管理员限当前租户已授权菜单
     * @return 菜单树
     */
    @Override
    public ResultData<List<MenuTree>> queryRoleMenuTreeList() {
        ResultData<List<MenuTree>> result = new ResultData<>();
        if (!SessionHolder.isSysAdmin() && !SessionHolder.isOrgAdmin()) {
            result.setErrorCode(ErrorCodeEnum.NO_PERMISSION);
            return result;
        }
        List<Integer> platformList = List.of(PLATFORM_TENANT);
        List<Menu> menuList = menuDao.queryMenuListByPlatform(platformList);
        if (!SessionHolder.isSysAdmin()) {
            Long tenantId = SessionHolder.getCurrentTenantId();
            List<Long> tenantMenuIds = tenantMenuDao.queryMenuIdsByTenantId(tenantId);
            Set<Long> tenantMenuIdSet = new HashSet<>(tenantMenuIds);
            menuList.removeIf(menu -> !tenantMenuIdSet.contains(menu.getId()));
        }
        List<MenuTree> treeList = this.buildMenuTree(menuList);
        result.setData(treeList);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 查询租户菜单授权树，含租户与组织平台菜单与按钮，仅站点管理员
     * @return 菜单树
     */
    @Override
    public ResultData<List<MenuTree>> queryTenantMenuTreeList() {
        ResultData<List<MenuTree>> result = new ResultData<>();
        if (!SessionHolder.isSysAdmin()) {
            result.setErrorCode(ErrorCodeEnum.NO_PERMISSION);
            return result;
        }
        List<Integer> platformList = List.of(PLATFORM_TENANT, PLATFORM_ORG);
        List<Menu> menuList = menuDao.queryMenuListByPlatform(platformList);
        List<MenuTree> treeList = this.buildMenuTree(menuList);
        result.setData(treeList);
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
        for (List<MenuTree> children : childrenMap.values()) {
            for (MenuTree node : children) {
                List<MenuTree> subChildren = childrenMap.getOrDefault(node.getCode(), new ArrayList<>());
                node.setChildren(subChildren);
            }
        }
        List<MenuTree> rootMenus = childrenMap.getOrDefault(MenuTree.ROOT_PARENT_CODE, new ArrayList<>());
        treeList.addAll(rootMenus);
        return treeList;
    }

    /**
     * 校验菜单参数，含标识码唯一与上级合法性
     * @param menuVO 菜单参数
     * @return 校验结果
     */
    private ResultData<Void> validateMenu(MenuVO menuVO) {
        ResultData<Void> result = new ResultData<>();
        if (menuVO == null || StringUtil.isBlank(menuVO.getName()) || StringUtil.isBlank(menuVO.getCode())) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        MenuTypeEnum menuType = MenuTypeEnum.indexOf(menuVO.getType());
        if (menuType == MenuTypeEnum.UNKNOWN || menuVO.getPlatform() == null
                || MenuPlatformEnum.indexOf(menuVO.getPlatform()) == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        if (menuType == MenuTypeEnum.MENU
                && (StringUtil.isBlank(menuVO.getPath()) || StringUtil.isBlank(menuVO.getComponent()))) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        if (menuType == MenuTypeEnum.BUTTON && StringUtil.isBlank(menuVO.getPerms())) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        // 标识码唯一，更新时排除自身
        Menu sameCodeMenu = menuDao.queryMenuByCode(menuVO.getCode());
        if (sameCodeMenu != null && !sameCodeMenu.getId().equals(menuVO.getId())) {
            result.setErrorCode(ErrorCodeEnum.MENU_CODE_EXIST);
            return result;
        }
        String parentCode = menuVO.getParentCode();
        if (StringUtil.isNotBlank(parentCode)) {
            if (parentCode.equals(menuVO.getCode())) {
                result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
                return result;
            }
            Menu parentMenu = menuDao.queryMenuByCode(parentCode);
            if (parentMenu == null || MenuTypeEnum.indexOf(parentMenu.getType()) == MenuTypeEnum.BUTTON) {
                result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
                return result;
            }
            // 父子菜单平台归属须一致，避免跨平台挂载污染身份路由与授权树
            if (!menuVO.getPlatform().equals(parentMenu.getPlatform())) {
                result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
                return result;
            }
            if (menuVO.getId() != null && this.hasAncestorCycle(parentMenu, menuVO.getCode())) {
                result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
                return result;
            }
        }
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 判断上级祖先链是否包含自身标识码，防止更新上级时成环
     * @param parentMenu 上级菜单
     * @param selfCode 自身标识码
     * @return true 成环
     */
    private boolean hasAncestorCycle(Menu parentMenu, String selfCode) {
        Menu current = parentMenu;
        int depth = 0;
        while (current != null && depth < MAX_MENU_DEPTH) {
            if (selfCode.equals(current.getCode())) {
                return true;
            }
            String parentCode = current.getParentCode();
            if (StringUtil.isBlank(parentCode)) {
                return false;
            }
            current = menuDao.queryMenuByCode(parentCode);
            depth++;
        }
        return false;
    }

    /**
     * 补齐菜单非空字段默认值，对齐表结构默认
     * @param menu 菜单
     */
    private void applyMenuDefaults(Menu menu) {
        if (menu.getParentCode() == null) {
            menu.setParentCode(MenuTree.ROOT_PARENT_CODE);
        }
        if (menu.getOrderNum() == null) {
            menu.setOrderNum(99);
        }
        if (menu.getVisible() == null) {
            menu.setVisible(1);
        }
        if (menu.getStatus() == null) {
            menu.setStatus(StatusEnum.NORMAL.getValue());
        }
    }
}
