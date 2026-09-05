package com.spark.dao.sys;

import com.spark.common.bean.sys.entity.UserProfile;
import com.spark.dao.BaseDao;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-08-25 12:00:00
 * 用户扩展信息DAO
 */
public interface UserProfileDao extends BaseDao<UserProfile> {

    /**
     * 插入数据
     * @param profile 用户扩展信息
     * @return 影响行数
     */
    @Override
    int insert(UserProfile profile);

    /**
     * 更新数据
     * @param profile 用户扩展信息
     * @return 影响行数
     */
    @Override
    int updateById(UserProfile profile);

    /**
     * 根据企业微信用户ID查询
     * @param wecomId 企业微信用户ID
     * @return 用户扩展信息
     */
    @Select("select * from sys_user_profile where wecom_id = #{wecomId} and delete_flag = 1 limit 1")
    UserProfile queryByWecomId(@Param("wecomId") String wecomId);

    /**
     * 根据微信openid查询
     * @param wxOpenId 微信openid
     * @return 用户扩展信息
     */
    @Select("select * from sys_user_profile where wx_openid = #{wxOpenId} and delete_flag = 1 limit 1")
    UserProfile queryByWxOpenId(@Param("wxOpenId") String wxOpenId);

    /**
     * 根据用户ID查询
     * @param userId 用户ID
     * @return 用户扩展信息
     */
    @Select("select * from sys_user_profile where id = #{userId} and delete_flag = 1 limit 1")
    UserProfile queryByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID删除（逻辑删除）
     * @param userId 用户ID
     * @param updatedBy 更新人
     * @return 影响行数
     */
    int deleteByUserId(@Param("userId") Long userId, @Param("updatedBy") Long updatedBy);
}
