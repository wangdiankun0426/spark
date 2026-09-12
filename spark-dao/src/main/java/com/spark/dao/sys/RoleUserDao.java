package com.spark.dao.sys;

import com.spark.common.bean.sys.query.RoleUserQuery;
import com.spark.common.bean.sys.result.RoleUserResult;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/2/27 20:16
 */
public interface RoleUserDao {

    /**
     * 查询列表
     * @param roleUserQuery
     * @return
     */
    List<RoleUserResult> queryRoleUserList(RoleUserQuery roleUserQuery);

    /**
     * 查询条数
     * @param query
     * @return
     */
    int queryRoleUserCount(RoleUserQuery query);

    /**
     * 批量插入
     * @param tenantId
     * @param roleId
     * @param userIds
     * @param createdBy
     * @return
     */
    int batchInsertByRoleId(@Param("tenantId")Long tenantId, @Param("roleId") Long roleId,
                            @Param("userIds") List<Long> userIds, @Param("createdBy") Long createdBy);

    /**
     * 批量插入
     * @param tenantId
     * @param userId
     * @param roleIds
     * @param createdBy
     * @return
     */
    int batchInsertByUserId(@Param("tenantId")Long tenantId, @Param("userId") Long userId,
                            @Param("roleIds") List<Long> roleIds, @Param("createdBy") Long createdBy);

    /**
     * 删除关联
     * @param roleIds
     * @param userId
     * @param updatedBy
     * @return
     */
    int deleteRoleUser(@Param("roleIds")List<Long> roleIds, @Param("userId") Long userId, @Param("updatedBy") Long updatedBy);

}
