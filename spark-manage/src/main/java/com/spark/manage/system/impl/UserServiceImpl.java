package com.spark.manage.system.impl;

import com.spark.config.aspectj.annotation.OperateLog;
import com.spark.bean.system.entity.User;
import com.spark.bean.system.query.*;
import com.spark.bean.system.result.*;
import com.spark.bean.system.vo.LoginVO;
import com.spark.bean.system.vo.UserVO;
import com.spark.bean.base.PageResult;
import com.spark.bean.base.ResultData;
import com.spark.bean.base.SessionHolder;
import com.spark.constant.ObjectCacheKey;
import com.spark.dao.system.*;
import com.spark.enums.ErrorCodeEnum;
import com.spark.enums.ObjectTypeEnum;
import com.spark.enums.OperateTypeEnum;
import com.spark.enums.StatusEnum;
import com.spark.manage.system.IDepartmentService;
import com.spark.manage.system.IUserService;
import com.spark.manage.BaseService;
import com.spark.config.redis.RedisService;
import com.spark.utils.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/2/17 18:52
 */
@Service
public class UserServiceImpl extends BaseService<UserQuery, UserResult> implements IUserService, InitializingBean {
    private final static Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    @Autowired
    private UserDao userDao;
    @Autowired
    private DepartmentDao departmentDao;
    @Autowired
    private RoleUserDao roleUserDao;
    @Autowired
    private RedisService redisService;
    @Autowired
    private IDepartmentService departmentService;
    @Value("${encrypt.privateKey}")
    private String privateKey;
    @Value("${user.avatar.path}")
    private String userAvatarPath;
    @Value("${default.user.password}")
    private String defaultUserPassword;

    /**
     * 创建用户
     * @param userVO 用户参数
     * @return 创建结果
     */
    @Override
    @OperateLog(operateType = OperateTypeEnum.USER_INSERT)
    public ResultData<Void> createUser(UserVO userVO) {
        ResultData<Void> result = this.validateCreateUserParam(userVO);
        if (result.getCode() != ResultData.OK) {
            return result;
        }
        User user = new User();
        Long userId = super.genObjectId(ObjectTypeEnum.USER);
        user.setId(userId);
        user.setLoginName(userVO.getLoginName());
        String password = EncryptUtil.md5(defaultUserPassword);
        user.setPassword(password);
        user.setName(userVO.getName());
        user.setDeptId(userVO.getDeptId());
        user.setPhone(userVO.getPhone());
        user.setEmail(userVO.getEmail());
        user.setSex(userVO.getSex());
        user.setStatus(userVO.getStatus());
        user.setWecomId(userVO.getWecomId());
        if (user.getStatus() == null) {
            user.setStatus(StatusEnum.NORMAL.getValue());
        }
        int count = userDao.insertDB(user);
        if (count < 1) {
            result.setErrorCode(ErrorCodeEnum.SYSTEM_ERROR);
            return result;
        }
        userVO.setId(userId);
        result = this.addUserRole(userVO);
        result.setObjId(userId);
        return result;
    }

    /**
     * 分页查询用户列表
     * @param query 查询参数
     * @return 结果
     */
    @Override
    public ResultData<PageResult<UserResult>> pageUserList(UserQuery query) {
        ResultData<PageResult<UserResult>> result = new ResultData<>();
        if (query == null) {
            query = new UserQuery();
        }
        PageResult<UserResult> pageResult = super.pageList(query);
        result.setData(pageResult);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 修改用户
     * @param userVO 修改的参数
     * @return 修改结果
     */
    @Override
    @OperateLog(operateType = OperateTypeEnum.USER_UPDATE)
    public ResultData<Void> updateUser(UserVO userVO) {
        ResultData<Void> result = this.validateUpdateUserParam(userVO);
        if (result.getCode() != ResultData.OK) {
            return result;
        }
        User user = new User();
        user.setId(userVO.getId());
        user.setName(userVO.getName());
        user.setLoginName(userVO.getLoginName());
        user.setDeptId(userVO.getDeptId());
        user.setPhone(userVO.getPhone());
        user.setEmail(userVO.getEmail());
        user.setSex(userVO.getSex());
        user.setStatus(userVO.getStatus());
        user.setWecomId(userVO.getWecomId());
        int count = userDao.updateDBById(user);
        if (count < 0) {
            return result;
        }
        result.setObjId(userVO.getId());
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 删除用户
     * @param userVO 删除的参数
     * @return 删除结果
     */
    @Override
    @OperateLog(operateType = OperateTypeEnum.USER_DELETE)
    public ResultData<Void> deleteUser(UserVO userVO) {
        ResultData<Void> result = new ResultData<>();
        if (userVO == null || userVO.getId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        User user = new User();
        user.setId(userVO.getId());
        int count = userDao.deleteDBById(user);
        if (count < 0) {
            return result;
        }
        result.setObjId(userVO.getId());
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 查询用户详情
     * @param userVO 查询参数
     * @return 查询结果
     */
    @Override
    public ResultData<UserResult> queryUserDetail(UserVO userVO) {
        ResultData<UserResult> result = new ResultData<>();
        if (userVO.getId() == null) {
            userVO.setId(SessionHolder.getCurrentUserId());
        }
        if (userVO.getId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        UserQuery userQuery = new UserQuery();
        userQuery.setId(userVO.getId());
        UserResult userResult = userDao.queryUser(userQuery);
        if (userResult == null) {
            result.setErrorCode(ErrorCodeEnum.USER_NOT_EXIST);
            return result;
        }
        userResult.setPassword(null);
        this.supplyUserDeptInfo(userResult);
        this.supplyUserRole(userResult);
        result.setData(userResult);
        result.setCode(ResultData.OK);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 强制退出
     * @param loginVO 退出的参数
     * @return 退出结果
     */
    @Override
    public ResultData<Void> forceLogout(LoginVO loginVO) {
        ResultData<Void> result = new ResultData<>();
        if (loginVO == null || StringUtil.isBlank(loginVO.getSessionId())) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        String sessionIdKey = ObjectCacheKey.LOGIN_SESSION + loginVO.getSessionId();
        boolean bo = redisService.del(sessionIdKey);
        if (!bo)  {
            return result;
        }
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 修改密码
     * @param userVO 修改的参数
     * @return 修改结果
     */
    @Override
    @OperateLog(operateType = OperateTypeEnum.USER_UPDATE_PASSWORD)
    public ResultData<Void> updatePassword(UserVO userVO) {
        ResultData<Void> result  = new ResultData<>();
        if (StringUtil.isBlank(userVO.getOldPassword()) || StringUtil.isBlank(userVO.getNewPassword())) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        Long userId = SessionHolder.getCurrentUserId();
        if (userId == null) {
            result.setErrorCode(ErrorCodeEnum.NOT_LOGIN);
            return result;
        }
        UserQuery userQuery = new UserQuery();
        userQuery.setId(userId);
        UserResult userResult = userDao.queryUser(userQuery);
        if (userResult == null) {
            result.setErrorCode(ErrorCodeEnum.USER_NOT_EXIST);
            return result;
        }
        String oldDes = DecryptUtil.des(userVO.getOldPassword(), privateKey);
        String oldMd5 = EncryptUtil.md5(oldDes);
        if (!userResult.getPassword().equals(oldMd5)) {
            result.setErrorCode(ErrorCodeEnum.LONG_PASSWORD_ERROR);
            return result;
        }
        String newDes = DecryptUtil.des(userVO.getNewPassword(), privateKey);
        String newMd5 = EncryptUtil.md5(newDes);
        User user = new User();
        user.setId(userId);
        user.setPassword(newMd5);
        int count = userDao.updateById(user);
        if (count < 1) {
            return result;
        }
        result.setObjId(userId);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 查询用户头像路径
     * @param userVO 查询参数
     * @return 头像路径
     */
    @Override
    public ResultData<String> queryUserAvatarPath(UserVO userVO) {
        ResultData<String> result = new ResultData<>();
        if (userVO.getId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        UserQuery userQuery = new UserQuery();
        userQuery.setId(userVO.getId());
        UserResult userResult = userDao.queryUser(userQuery);
        if (userResult == null) {
            return result;
        }
        String avatar = userResult.getAvatar();
        String userAvatar = userAvatarPath + "default.jpg";
        if (StringUtil.isNotBlank(avatar)) {
            userAvatar = userAvatarPath + avatar;
            File file = new File(userAvatar);
            if (!file.exists()) {
                userAvatar = userAvatarPath + "default.jpg";
            }
        }
        result.setData(userAvatar);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 上传用户头像
     * @param file  文件
     * @return 上传结果
     */
    @Override
    public ResultData<Void> uploadAvatar(MultipartFile file) {
        ResultData<Void> result = new ResultData<>();
        if (file == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        Long userId = SessionHolder.getCurrentUserId();
        if (userId == null) {
            result.setErrorCode(ErrorCodeEnum.NOT_LOGIN);
            return result;
        }
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String path = userAvatarPath + uuid;
        try {
            file.transferTo(new File(path));
        } catch (IOException e) {
            logger.error("uploadAvatar error is ", e);
            return result;
        }
        User user = new User();
        user.setId(userId);
        user.setAvatar(uuid);
        int count = userDao.updateById(user);
        if (count < 1) {
            return result;
        }
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 补充部门数据
     * @param userResult  用户
     */
    private void supplyUserDeptInfo(UserResult userResult) {
        Long deptId = userResult.getDeptId();
        if (deptId == null) {
            return;
        }
        DepartmentQuery departmentQuery = new DepartmentQuery();
        departmentQuery.setId(deptId);
        DepartmentResult departmentResult = departmentDao.queryDepartment(departmentQuery);
        if (departmentResult == null) {
            return;
        }
        userResult.setDeptName(departmentResult.getName());
        ResultData<String> deptPathData = departmentService.supplyDeptPath(departmentResult);
        if (deptPathData.getCode() != ResultData.OK) {
            return;
        }
        userResult.setDeptPath(deptPathData.getData());
    }

    /**
     * 补充用户角色
     * @param userResult  用户
     */
    private void supplyUserRole(UserResult userResult) {
        Long userId = userResult.getId();
        RoleUserQuery roleUserQuery = new RoleUserQuery();
        roleUserQuery.setUserId(userId);
        List<RoleUserResult> roleUserList = roleUserDao.queryRoleUserList(roleUserQuery);
        if (CollectionUtil.isEmpty(roleUserList)) {
            return;
        }
        List<Long> roleIds = roleUserList.stream().map(RoleUserResult::getRoleId).distinct().collect(Collectors.toList());
        userResult.setRoleIds(roleIds);
    }

    /**
     * 验证创建用户参数
     * @param userVO 创建用户参数
     * @return 验证结果
     */
    private ResultData<Void> validateCreateUserParam(UserVO userVO) {
        ResultData<Void> result = new ResultData<>();
        if (userVO == null || StringUtil.isBlank(userVO.getLoginName()) || StringUtil.isBlank(userVO.getName())) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        UserQuery userQuery = new UserQuery();
        userQuery.setLoginName(userVO.getLoginName());
        UserResult userResult = userDao.queryUser(userQuery);
        if (userResult != null) {
            result.setErrorCode(ErrorCodeEnum.USER_SAME_LOGIN_NAME_EXIST);
            return result;
        }
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 验证修改用户参数
     * @param userVO 修改用户参数
     * @return 验证结果
     */
    private ResultData<Void> validateUpdateUserParam(UserVO userVO) {
        ResultData<Void> result = new ResultData<>();
        if (userVO == null || userVO.getId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        if ( StringUtil.isBlank(userVO.getLoginName())) {
            result.setCode(ResultData.OK);
            return result;
        }
        UserQuery userQuery = new UserQuery();
        userQuery.setLoginName(userVO.getLoginName());
        UserResult userResult = userDao.queryUser(userQuery);
        if (userResult != null && !userResult.getId().equals(userVO.getId())) {
            result.setErrorCode(ErrorCodeEnum.USER_SAME_LOGIN_NAME_EXIST);
            return result;
        }
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 补充用户列表数据
     * @param list 列表数据
     */

    @Override
    protected void supplyList(List<UserResult> list) {
        if (CollectionUtil.isEmpty(list)) {
            return;
        }
        super.supplyCreatedByName(list);
        super.supplyUpdatedByName(list);
    }

    /**
     * 添加用户角色
     * @param userVO 创建用户参数
     * @return 验证结果
     */
    private ResultData<Void> addUserRole(UserVO userVO) {
        ResultData<Void> result = new ResultData<>();
        if (userVO.getId() == null || CollectionUtil.isEmpty(userVO.getRoleIds())) {
           result.setCode(ResultData.OK);
           return result;
        }
        Long userId = userVO.getId();
        List<Long> roleIds = userVO.getRoleIds();
        Long createdBy = SessionHolder.getCurrentUserId();
        int count = roleUserDao.batchInsertByUserId(userId, roleIds, createdBy);
        if (count < 1) {
            return result;
        }
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 分页查询总数
     * @param query 查询参数
     * @return 总数
     */
    @Override
    protected int queryCount(UserQuery query){
        return userDao.queryUserCount(query);
    }

    /**
     * 查询列表
     * @param query 查询参数
     * @return 列表
     */
    @Override
    protected List<UserResult> queryList(UserQuery query){
        return userDao.queryUserList(query);
    }

    /**
     * 查询最大ID
     * @return 最大ID
     */
    @Override
    protected Long queryMaxId() {
        return userDao.queryUserMaxId();
    }

    /**
     * 初始化
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        String defaultAvatar = userAvatarPath + "default.jpg";
        File file = new File(defaultAvatar);
        if (!file.exists()) {
            InputStream is = new ClassPathResource("avatar/default.jpg").getInputStream();
            Files.copy(is, Paths.get(defaultAvatar), StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
