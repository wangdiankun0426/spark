package com.spark.dao.sys;

import com.spark.common.bean.sys.entity.Menu;
import com.spark.common.bean.sys.query.MenuQuery;
import com.spark.dao.BaseDao;
import org.apache.ibatis.annotations.Param;

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
public interface MenuDao extends BaseDao<Menu> {

    /**
     * 查询菜单列表
     * @return 菜单列表
     */
    List<Menu> queryMenuList();

    /**
     * 按平台归属查询启用菜单列表，含目录菜单按钮全部类型
     * @param platformList 平台归属集合
     * @return 菜单列表
     */
    List<Menu> queryMenuListByPlatform(@Param("platformList") List<Integer> platformList);

    /**
     * 查询用户可见菜单列表，角色授权与公共菜单并集，限租户业务平台
     * @param tenantId 租户id
     * @param userId 用户id
     * @return 菜单列表
     */
    List<Menu> queryUserMenuList(@Param("tenantId") Long tenantId, @Param("userId") Long userId);

    /**
     * 查询站点管理员可见菜单列表，仅站点平台菜单
     * @return 菜单列表
     */
    List<Menu> querySysAdminMenuList();

    /**
     * 查询组织管理员已勾选的组织菜单列表，仅组织平台启用菜单，经租户用户定位关联
     * @param tenantId 租户id
     * @param userId 用户id
     * @return 菜单列表
     */
    List<Menu> queryOrgAdminMenuList(@Param("tenantId") Long tenantId, @Param("userId") Long userId);

    /**
     * 查询单个菜单
     * @param query 查询参数，按id
     * @return 菜单
     */
    Menu queryMenu(MenuQuery query);

    /**
     * 按标识码查询菜单，用于标识码唯一性校验
     * @param code 标识码
     * @return 菜单
     */
    Menu queryMenuByCode(@Param("code") String code);

    /**
     * 统计直接子菜单数量，删除前校验
     * @param parentCode 上级菜单标识码
     * @return 子菜单数量
     */
    int queryMenuCountByParentCode(@Param("parentCode") String parentCode);

    /**
     * 物理删除菜单，code唯一键下逻辑删除会阻塞同码重建故物理删除
     * @param id 菜单id
     * @return 影响行数
     */
    int deleteMenuById(@Param("id") Long id);
}
