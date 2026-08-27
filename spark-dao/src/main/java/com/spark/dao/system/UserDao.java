package com.spark.dao.system;

import com.spark.bean.system.result.UserResult;
import com.spark.bean.system.entity.User;
import com.spark.bean.system.query.UserQuery;
import com.spark.dao.BaseDao;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/2/17 18:55
 */
public interface UserDao extends BaseDao<User> {

    /**
     * 查询最大id
     * @return
     */
    @Select("select max(id) from sys_user")
    Long queryUserMaxId();

    /**
     * 插入数据
     * @param user
     * @return
     */
    @Override
    int insert(User user);

    /**
     * 查询列表
     * @param query
     * @return
     */
    List<UserResult> queryUserList(UserQuery query);

    /**
     * 查询数量
     * @param query
     * @return
     */
    int queryUserCount(UserQuery query);

    /**
     * 查询单条
     * @param query
     * @return
     */
    UserResult queryUser(UserQuery query);

    /**
     * 删除用户 根据id
     * @param user
     * @return
     */
    @Override
    int deleteById(User user);

    /**
     * 修改用户 根据id
     * @param user
     * @return
     */
    @Override
    int updateById(User user);

    /**
     * 重置用户的部门
     * @param deptId
     * @return
     */
    @Update("update sys_user set dept_id = null where dept_id = #{deptId}")
    int resetUserDeptByDeptId(@Param("deptId") Long deptId);
}
