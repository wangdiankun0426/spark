package com.spark.manage.sys;

import com.spark.common.bean.sys.query.RoleQuery;
import com.spark.common.bean.sys.result.RoleResult;
import com.spark.common.bean.sys.vo.RoleVO;
import com.spark.common.bean.base.PageResult;
import com.spark.common.bean.base.ResultData;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/2/25 21:36
 */
public interface IRoleService {

    /**
     * 创建角色
     * @param roleVO 角色参数
     * @return 创建结果
     */
    ResultData<Void> createRole(RoleVO roleVO);

    /**
     * 分页查询列表
     * @param query 查询参数
     * @return 结果
     */
    ResultData<PageResult<RoleResult>> pageRoleList(RoleQuery query);

    /**
     * 修改角色
     * @param roleVO 修改的参数
     * @return 修改结果
     */
    ResultData<Void> updateRole(RoleVO roleVO);

    /**
     * 删除角色
     * @param roleVO 删除的参数
     * @return 删除结果
     */
    ResultData<Void> deleteRole(RoleVO roleVO);
}
