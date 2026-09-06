package com.spark.dao.sys;

import com.spark.common.bean.sys.entity.Menu;
import com.spark.dao.BaseDao;

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
}
