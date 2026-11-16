package com.spark.manage.sys.impl;

import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.base.SessionHolder;
import com.spark.common.bean.sys.entity.Menu;
import com.spark.common.bean.sys.entity.Session;
import com.spark.common.bean.sys.tree.MenuTree;
import com.spark.common.bean.sys.vo.RouterVO;
import com.spark.common.constant.ObjectCacheKey;
import com.spark.common.enums.AccountTypeEnum;
import com.spark.common.enums.DataScopeEnum;
import com.spark.common.enums.MenuTypeEnum;
import com.spark.common.enums.RoleTypeEnum;
import com.spark.common.enums.StatusEnum;
import com.spark.common.utils.CollectionUtil;
import com.spark.common.utils.StringUtil;
import com.spark.dao.sys.MenuDao;
import com.spark.dao.sys.RoleDao;
import com.spark.manage.sys.IPermissionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
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
 * @since 2026-11-13 15:00:00
 * 权限加载服务实现
 * 站点管理员授通配权限，组织管理员物化租户授权组织菜单、角色授权与公共菜单并集，普通用户取公共菜单与租户授权范围内角色授权菜单并集
 */
@Service
public class PermissionServiceImpl implements IPermissionService {
    private final static Logger logger = LoggerFactory.getLogger(PermissionServiceImpl.class);
    /**
     * 站点管理员通配权限串
     */
    private static final String ALL_PERMISSION = "*:*:*";
    @Autowired
    private MenuDao menuDao;
    @Autowired
    private RoleDao roleDao;

    /**
     * 按身份加载权限串集合与数据范围到会话
     * @param session 登录会话
     */
    @Override
    public void loadPermission(Session session) {
        if (session == null || session.getUserId() == null) {
            logger.warn("loadPermission skip, invalid session");
            return;
        }
        session.setSessionVersion(ObjectCacheKey.SESSION_VERSION);
        Integer accountType = session.getAccountType();
        Integer accountTypeValue = AccountTypeEnum.indexOf(accountType).getValue();
        Integer siteAdminValue = AccountTypeEnum.SITE_ADMIN.getValue();
        boolean sysAdmin = siteAdminValue.equals(accountTypeValue);
        if (sysAdmin) {
            Set<String> permissions = new HashSet<>();
            permissions.add(ALL_PERMISSION);
            session.setPermissions(permissions);
            this.applyAllAccess(session);
            return;
        }
        Integer roleType = session.getRoleType();
        boolean orgAdmin = RoleTypeEnum.hasRole(roleType, RoleTypeEnum.ORG_ADMIN);
        if (orgAdmin) {
            Long orgTenantId = session.getTenantId();
            Long orgUserId = session.getUserId();
            List<Menu> orgMenuList = menuDao.queryTenantOrgMenuList(orgTenantId);
            List<Menu> userMenuList = menuDao.queryUserMenuList(orgTenantId, orgUserId);
            List<Menu> menuList = new ArrayList<>(orgMenuList);
            menuList.addAll(userMenuList);
            Set<String> permissions = this.extractPermissions(menuList);
            session.setPermissions(permissions);
            this.applyAllAccess(session);
            return;
        }
        Long tenantId = session.getTenantId();
        Long userId = session.getUserId();
        List<Menu> menuList = menuDao.queryUserMenuList(tenantId, userId);
        Set<String> permissions = this.extractPermissions(menuList);
        session.setPermissions(permissions);
        List<Integer> dataScopeList = roleDao.queryUserDataScopeList(tenantId, userId);
        if (CollectionUtil.isEmpty(dataScopeList)) {
            dataScopeList = new ArrayList<>();
            Integer onlyOneself = DataScopeEnum.ONLY_ONESELF.getValue();
            dataScopeList.add(onlyOneself);
        }
        session.setDataScopeList(dataScopeList);
        List<Long> customDeptIdList = roleDao.queryUserCustomDeptIds(tenantId, userId);
        String customDeptIds = StringUtil.join(customDeptIdList, ",");
        session.setCustomDeptIds(customDeptIds);
    }

    /**
     * 构建当前登录用户的动态路由树
     * @return 路由树
     */
    @Override
    public ResultData<List<RouterVO>> buildRouters() {
        ResultData<List<RouterVO>> result = new ResultData<>();
        List<Menu> menuList = this.queryMenuListByIdentity();
        List<RouterVO> routers = this.buildRouterTree(menuList);
        result.setData(routers);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 按当前身份查询可见菜单列表
     * @return 菜单列表
     */
    private List<Menu> queryMenuListByIdentity() {
        if (SessionHolder.isSysAdmin()) {
            return menuDao.querySysAdminMenuList();
        }
        if (SessionHolder.isOrgAdmin()) {
            Long tenantId = SessionHolder.getCurrentTenantId();
            Long userId = SessionHolder.getCurrentUserId();
            List<Menu> orgMenuList = menuDao.queryTenantOrgMenuList(tenantId);
            List<Menu> userMenuList = menuDao.queryUserMenuList(tenantId, userId);
            List<Menu> menuList = new ArrayList<>(orgMenuList);
            menuList.addAll(userMenuList);
            return this.includeAncestorMenus(menuList);
        }
        Long tenantId = SessionHolder.getCurrentTenantId();
        Long userId = SessionHolder.getCurrentUserId();
        List<Menu> menuList = menuDao.queryUserMenuList(tenantId, userId);
        return this.includeAncestorMenus(menuList);
    }

    /**
     * 补全用户菜单的祖先目录，避免角色仅授叶子菜单时建树丢父
     * @param menuList 用户可见菜单列表
     * @return 含祖先目录的菜单列表
     */
    private List<Menu> includeAncestorMenus(List<Menu> menuList) {
        if (CollectionUtil.isEmpty(menuList)) {
            return menuList;
        }
        List<Menu> allMenuList = menuDao.queryMenuList();
        Map<String, Menu> allMenuMap = new LinkedHashMap<>();
        for (Menu menu : allMenuList) {
            allMenuMap.put(menu.getCode(), menu);
        }
        Map<String, Menu> mergedMap = new LinkedHashMap<>();
        for (Menu menu : menuList) {
            mergedMap.put(menu.getCode(), menu);
        }
        for (Menu menu : menuList) {
            String parentCode = menu.getParentCode();
            while (StringUtil.isNotBlank(parentCode) && !mergedMap.containsKey(parentCode)) {
                Menu parent = allMenuMap.get(parentCode);
                if (parent == null || !StatusEnum.NORMAL.getValue().equals(parent.getStatus())) {
                    break;
                }
                mergedMap.put(parent.getCode(), parent);
                parentCode = parent.getParentCode();
            }
        }
        List<Menu> mergedList = new ArrayList<>();
        mergedList.addAll(mergedMap.values());
        Comparator<Menu> menuComparator = Comparator.comparing(Menu::getOrderNum).thenComparing(Menu::getId);
        mergedList.sort(menuComparator);
        return mergedList;
    }

    /**
     * 按上级菜单标识码构建路由树，过滤按钮节点
     * @param menuList 菜单列表
     * @return 路由树
     */
    private List<RouterVO> buildRouterTree(List<Menu> menuList) {
        List<RouterVO> treeList = new ArrayList<>();
        if (CollectionUtil.isEmpty(menuList)) {
            return treeList;
        }
        Map<String, List<RouterVO>> childrenMap = new LinkedHashMap<>();
        for (Menu menu : menuList) {
            Integer buttonValue = MenuTypeEnum.BUTTON.getValue();
            if (buttonValue.equals(menu.getType())) {
                continue;
            }
            RouterVO router = this.toRouter(menu);
            String parentCode = StringUtil.isBlank(menu.getParentCode())
                    ? MenuTree.ROOT_PARENT_CODE : menu.getParentCode();
            childrenMap.computeIfAbsent(parentCode, k -> new ArrayList<>()).add(router);
        }
        childrenMap.forEach((parentCode, children) -> children.forEach(node -> {
            String code = node.getCode();
            List<RouterVO> childList = childrenMap.getOrDefault(code, new ArrayList<>());
            node.setChildren(childList);
        }));
        List<RouterVO> rootList = childrenMap.getOrDefault(MenuTree.ROOT_PARENT_CODE, new ArrayList<>());
        treeList.addAll(rootList);
        return treeList;
    }

    /**
     * 菜单转路由节点
     * @param menu 菜单
     * @return 路由节点
     */
    private RouterVO toRouter(Menu menu) {
        RouterVO router = new RouterVO();
        router.setName(menu.getCode());
        router.setPath(menu.getPath());
        router.setComponent(menu.getComponent());
        router.setTitle(menu.getName());
        router.setIcon(menu.getIcon());
        router.setCode(menu.getCode());
        router.setOrderNum(menu.getOrderNum());
        Integer visible = menu.getVisible();
        boolean hidden = visible != null && visible == 0;
        router.setHidden(hidden);
        return router;
    }

    /**
     * 从菜单列表提取非空权限串集合
     * @param menuList 菜单列表
     * @return 权限串集合
     */
    private Set<String> extractPermissions(List<Menu> menuList) {
        Set<String> permissions = new HashSet<>();
        if (CollectionUtil.isEmpty(menuList)) {
            return permissions;
        }
        for (Menu menu : menuList) {
            String perms = menu.getPerms();
            if (StringUtil.isNotBlank(perms)) {
                permissions.add(perms);
            }
        }
        return permissions;
    }

    /**
     * 授予全部数据范围
     * @param session 登录会话
     */
    private void applyAllAccess(Session session) {
        Integer allAccess = DataScopeEnum.ALL_ACCESS.getValue();
        List<Integer> dataScopeList = new ArrayList<>();
        dataScopeList.add(allAccess);
        session.setDataScopeList(dataScopeList);
    }
}