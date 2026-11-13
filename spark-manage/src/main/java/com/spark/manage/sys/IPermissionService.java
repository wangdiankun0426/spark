package com.spark.manage.sys;

import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.sys.entity.Session;
import com.spark.common.bean.sys.vo.RouterVO;

import java.util.List;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-11-13 15:00:00
 * 权限加载服务，登录时物化操作权限与数据范围，会话期构建动态路由
 */
public interface IPermissionService {

    /**
     * 按身份加载权限串集合与数据范围到会话
     * @param session 登录会话
     */
    void loadPermission(Session session);

    /**
     * 构建当前登录用户的动态路由树
     * @return 路由树
     */
    ResultData<List<RouterVO>> buildRouters();
}