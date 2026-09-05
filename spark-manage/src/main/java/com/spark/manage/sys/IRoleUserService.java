package com.spark.manage.sys;

import com.spark.common.bean.sys.query.RoleUserQuery;
import com.spark.common.bean.sys.result.RoleUserResult;
import com.spark.common.bean.sys.vo.RoleUserVO;
import com.spark.common.bean.base.PageResult;
import com.spark.common.bean.base.ResultData;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/3/26 16:29
 */
public interface IRoleUserService {

    /**
     * 添加用户
     * @param roleUserVO 添加用户参数
     * @return 验证结果
     */
    ResultData<Void> addUser(RoleUserVO roleUserVO);

    /**
     * 移除用户
     * @param roleUserVO 移除用户参数
     * @return 验证结果
     */
    ResultData<Void> removeUser(RoleUserVO roleUserVO);

    /**
     * 分页查询角色用户列表
     * @param query 查询参数
     * @return 结果
     */
    ResultData<PageResult<RoleUserResult>> pageRoleUserList(RoleUserQuery query);
}
