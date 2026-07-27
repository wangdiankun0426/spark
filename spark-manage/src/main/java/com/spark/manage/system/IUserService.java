package com.spark.manage.system;

import com.spark.bean.system.query.UserQuery;
import com.spark.bean.system.result.UserResult;
import com.spark.bean.system.vo.LoginVO;
import com.spark.bean.system.vo.UserVO;
import com.spark.bean.base.PageResult;
import com.spark.bean.base.ResultData;
import org.springframework.web.multipart.MultipartFile;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/2/17 18:51
 */
public interface IUserService {

    /**
     * 创建用户
     * @param userVO 创建用户参数
     * @return 创建结果
     */
    ResultData<Void> createUser(UserVO userVO);

    /**
     * 分页查询用户列表
     * @param query 查询参数
     * @return 结果
     */
    ResultData<PageResult<UserResult>> pageUserList(UserQuery query);

    /**
     * 修改用户
     * @param userVO 修改用户参数
     * @return 修改结果
     */
    ResultData<Void> updateUser(UserVO userVO);

    /**
     * 删除用户
     * @param userVO 删除用户参数
     * @return 删除结果
     */
    ResultData<Void> deleteUser(UserVO userVO);

    /**
     * 查询用户详情
     * @param userVO 查询参数
     * @return 查询结果
     */
    ResultData<UserResult> queryUserDetail(UserVO userVO);

    /**
     * 强制退出
     * @param loginVO 退出的参数
     * @return 退出结果
     */
    ResultData<Void> forceLogout(LoginVO loginVO);

    /**
     * 修改密码
     * @param userVO 修改密码参数
     * @return
     */
    ResultData<Void> updatePassword(UserVO userVO);

    /**
     * 查询用户头像路径
     * @param userVO 查询参数
     * @return
     */
    ResultData<String> queryUserAvatarPath(UserVO userVO);

    /**
     * 上传用户头像
     * @param file  文件
     * @return
     */
    ResultData<Void> uploadAvatar(MultipartFile file);

}
