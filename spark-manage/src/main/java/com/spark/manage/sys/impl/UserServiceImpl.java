package com.spark.manage.sys.impl;

import com.spark.common.bean.base.BaseAssert;
import com.spark.common.bean.sys.query.DepartmentQuery;
import com.spark.common.bean.sys.query.RoleUserQuery;
import com.spark.common.bean.sys.query.TenantUserQuery;
import com.spark.common.bean.sys.query.UserQuery;
import com.spark.common.bean.sys.result.DepartmentResult;
import com.spark.common.bean.sys.result.RoleUserResult;
import com.spark.common.bean.sys.result.TenantUserResult;
import com.spark.common.bean.sys.result.UserResult;
import com.spark.common.bean.sys.vo.TenantUserVO;
import com.spark.common.enums.*;
import com.spark.common.utils.CollectionUtil;
import com.spark.common.utils.DecryptUtil;
import com.spark.common.utils.EncryptUtil;
import com.spark.common.utils.StringUtil;
import com.spark.config.aspectj.annotation.LogPrint;
import com.spark.config.aspectj.annotation.LogOperate;
import com.spark.common.bean.sys.entity.User;
import com.spark.common.bean.sys.entity.UserProfile;
import com.spark.common.bean.sys.vo.UserVO;
import com.spark.common.bean.base.PageResult;
import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.base.SessionHolder;
import com.spark.dao.sys.*;
import com.spark.manage.auth.IEncryptKeyService;
import com.spark.manage.sys.IDepartmentService;
import com.spark.manage.sys.ITenantConfigService;
import com.spark.manage.sys.ITenantUserService;
import com.spark.manage.sys.IUserService;
import com.spark.manage.BaseService;
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
@LogPrint
public class UserServiceImpl extends BaseService<UserQuery, UserResult> implements IUserService, InitializingBean {
    private final static Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    @Autowired
    private UserDao userDao;
    @Autowired
    private UserProfileDao userProfileDao;
    @Autowired
    private DepartmentDao departmentDao;
    @Autowired
    private RoleUserDao roleUserDao;
    @Autowired
    private RoleDao roleDao;
    @Autowired
    private IDepartmentService departmentService;
    @Autowired
    private ITenantUserService tenantUserService;
    @Autowired
    private TenantUserDao tenantUserDao;
    @Autowired
    private ITenantConfigService tenantConfigService;
    @Autowired
    private IEncryptKeyService encryptKeyService;
    @Value("${user.avatar.path}")
    private String userAvatarPath;

    /**
     * 创建用户
     * @param userVO 用户参数
     * @return 创建结果
     */
    @Override
    @LogOperate(operateType = OperateTypeEnum.USER_INSERT)
    public ResultData<Long> createUser(UserVO userVO) {
        ResultData<Long> result = this.validateCreateUserParam(userVO);
        BaseAssert.assertTrue(result);
        Long tenantId = SessionHolder.getCurrentTenantId();
        result = tenantUserService.checkTenantUserCount(tenantId);
        BaseAssert.assertTrue(result);
        // 插入主表数据
        User user = new User();
        Long userId = super.genObjectId(ObjectTypeEnum.USER);
        user.setId(userId);
        user.setLoginName(userVO.getLoginName());
        String password = userVO.getPassword();
        if (StringUtil.isBlank(password)) {
            ResultData<String> pwdResult = tenantConfigService.queryTenantConfigValue(tenantId, TenantConfigEnum.DEFAULT_PASSWORD.getKey());
            BaseAssert.assertTrue(pwdResult);
            password = EncryptUtil.md5(pwdResult.getData());
        }
        user.setPassword(password);
        user.setName(userVO.getName());
        user.setPhone(userVO.getPhone());
        user.setEmail(userVO.getEmail());
        user.setSex(userVO.getSex());
        user.setCurrentTenantId(tenantId);
        user.setStatus(userVO.getStatus());
        if (user.getStatus() == null) {
            user.setStatus(StatusEnum.NORMAL.getValue());
        }
        user.setAccountType(userVO.getAccountType());
        if (user.getAccountType() == null) {
            user.setAccountType(AccountTypeEnum.COMMON.getValue());
        }
        int count = userDao.insertDB(user);
        if (count < 1) {
            result.setErrorCode(ErrorCodeEnum.INSERT_DATA_FAIL);
            return result;
        }
        // 插入扩展信息
        UserProfile userProfile = new UserProfile();
        userProfile.setId(userId);
        userProfile.setWecomId(userVO.getWecomId());
        userProfile.setWxOpenId(userVO.getWxOpenId());
        userProfile.setWxUnionId(userVO.getWxUnionId());
        count = userProfileDao.insertDB(userProfile);
        if (count < 1) {
            result.setErrorCode(ErrorCodeEnum.INSERT_DATA_FAIL);
            return result;
        }
        // 加入租户
        TenantUserVO tenantUserVO = new TenantUserVO();
        tenantUserVO.setTenantId(SessionHolder.getCurrentTenantId());
        tenantUserVO.setDeptId(userVO.getDeptId());
        tenantUserVO.setUserIds(List.of(userId));
        result = tenantUserService.addUser(tenantUserVO);
        BaseAssert.assertTrue(result);
        userVO.setId(userId);
        // 添加角色
        result = this.addUserRole(userVO);
        BaseAssert.assertTrue(result);
        result.setObjId(userId);
        result.setData(userId);
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
        if (!SessionHolder.isSysAdmin()) {
            query.setTenantId(SessionHolder.getCurrentTenantId());
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
    @LogOperate(operateType = OperateTypeEnum.USER_UPDATE)
    public ResultData<Void> updateUser(UserVO userVO) {
        ResultData<Void> result = this.validateUpdateUserParam(userVO);
        BaseAssert.assertTrue(result);
        Long userId = userVO.getId();
        // 更新主表数据
        User user = new User();
        user.setId(userId);
        user.setName(userVO.getName());
        user.setLoginName(userVO.getLoginName());
        user.setPhone(userVO.getPhone());
        user.setEmail(userVO.getEmail());
        user.setSex(userVO.getSex());
        user.setStatus(userVO.getStatus());
        int count = userDao.updateDBById(user);
        if (count < 0) {
            result.setErrorCode(ErrorCodeEnum.UPDATE_DATA_FAIL);
            return result;
        }
        // 更新扩展表数据
        UserProfile userProfile = new UserProfile();
        userProfile.setId(userId);
        userProfile.setWecomId(userVO.getWecomId());
        count = userProfileDao.updateDBById(userProfile);
        if (count < 1) {
            result.setErrorCode(ErrorCodeEnum.UPDATE_DATA_FAIL);
            return result;
        }
        // 加入租户
        TenantUserVO tenantUserVO = new TenantUserVO();
        tenantUserVO.setTenantId(SessionHolder.getCurrentTenantId());
        tenantUserVO.setDeptId(userVO.getDeptId());
        tenantUserVO.setUserId(userId);
        result = tenantUserService.updateTenantUser(tenantUserVO);
        BaseAssert.assertTrue(result);
        // 添加角色
        result = this.addUserRole(userVO);
        BaseAssert.assertTrue(result);
        result.setObjId(userId);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 删除用户
     * @param userVO 删除的参数
     * @return 删除结果
     */
    @Override
    @LogOperate(operateType = OperateTypeEnum.USER_DELETE)
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
            result.setErrorCode(ErrorCodeEnum.DELETE_DATA_FAIL);
            return result;
        }
        // 删除用户所属租户信息
        tenantUserDao.deleteTenantUserV2(userVO.getId(), SessionHolder.getCurrentUserId());
        result.setObjId(userVO.getId());
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 查询用户详情
     * @param query 查询参数
     * @return 查询结果
     */
    @Override
    @LogOperate(operateType = OperateTypeEnum.USER_DETAIL)
    public ResultData<UserResult> queryUserDetail(UserQuery query) {
        ResultData<UserResult> result = new ResultData<>();
        if (query.getId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        UserResult userResult = userDao.queryUser(query);
        if (userResult == null) {
            result.setErrorCode(ErrorCodeEnum.USER_NOT_EXIST);
            return result;
        }
        userResult.setPassword(null);
        this.supplyUserTenantInfo(userResult);
        this.supplyUserDeptInfo(userResult);
        this.supplyUserRoleInfo(userResult);
        this.supplyUserMenu(userResult);
        result.setData(userResult);
        result.setObjId(query.getId());
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 修改密码
     * @param userVO 修改的参数
     * @return 修改结果
     */
    @Override
    @LogOperate(operateType = OperateTypeEnum.USER_UPDATE_PASSWORD)
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
        // 一次性消费密钥ID对应的密钥，本次改密两个字段共用
        String desKey = encryptKeyService.consumeEncryptKey(userVO.getKeyId());
        if (StringUtil.isBlank(desKey)) {
            result.setErrorCode(ErrorCodeEnum.ENCRYPT_KEY_INVALID);
            return result;
        }
        String oldDes = DecryptUtil.des(userVO.getOldPassword(), desKey);
        String newDes = DecryptUtil.des(userVO.getNewPassword(), desKey);
        if (StringUtil.isBlank(oldDes) || StringUtil.isBlank(newDes)) {
            result.setErrorCode(ErrorCodeEnum.ENCRYPT_KEY_INVALID);
            return result;
        }
        String oldMd5 = EncryptUtil.md5(oldDes);
        if (!userResult.getPassword().equals(oldMd5)) {
            result.setErrorCode(ErrorCodeEnum.LONG_PASSWORD_ERROR);
            return result;
        }
        String newMd5 = EncryptUtil.md5(newDes);
        User user = new User();
        user.setId(userId);
        user.setPassword(newMd5);
        int count = userDao.updateById(user);
        if (count < 1) {
            result.setErrorCode(ErrorCodeEnum.UPDATE_DATA_FAIL);
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
            result.setErrorCode(ErrorCodeEnum.UPDATE_DATA_FAIL);
            return result;
        }
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 补充租户数据
     * @param userResult
     */
    private void supplyUserTenantInfo(UserResult userResult) {
        TenantUserQuery tenantUserQuery = new TenantUserQuery();
        tenantUserQuery.setTenantId(SessionHolder.getCurrentTenantId());
        tenantUserQuery.setUserId(userResult.getId());
        TenantUserResult tenantUserResult = tenantUserDao.queryTenantUser(tenantUserQuery);
        if (tenantUserResult == null) {
            return;
        }
        userResult.setTenantId(tenantUserResult.getTenantId());
        userResult.setRoleType(tenantUserResult.getRoleType());
        userResult.setDeptId(tenantUserResult.getDeptId());
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
        if (deptPathData.getCode() == ResultData.OK) {
            userResult.setDeptPath(deptPathData.getData());
        }
    }

    /**
     * 补充用户角色信息
     * @param userResult  用户
     */
    private void supplyUserRoleInfo(UserResult userResult) {
        Long userId = userResult.getId();
        RoleUserQuery roleUserQuery = new RoleUserQuery();
        roleUserQuery.setTenantId(SessionHolder.getCurrentTenantId());
        roleUserQuery.setUserId(userId);
        roleUserQuery.setPage(false);
        List<RoleUserResult> roleUserList = roleUserDao.queryRoleUserList(roleUserQuery);
        if (CollectionUtil.isEmpty(roleUserList)) {
            return;
        }
        List<Long> roleIds = roleUserList.stream().map(RoleUserResult::getRoleId).distinct().collect(Collectors.toList());
        userResult.setRoleIds(roleIds);
        userResult.setRoleNames(StringUtil.join(super.getObjNames(roleIds), "、"));
    }

    /**
     * 补充用户菜单权限
     * @param userResult 用户
     */
    private void supplyUserMenu(UserResult userResult) {
        Long userId = userResult.getId();
        if (userId == null) {
            return;
        }
        List<String> menuCodesList = roleDao.queryUserMenuCodes(SessionHolder.getCurrentTenantId(), userId);
        if (CollectionUtil.isEmpty(menuCodesList)) {
            return;
        }
        Set<String> menuCodeSet = new HashSet<>();
        for (String menuCodes : menuCodesList) {
            String[] codeArray = menuCodes.split(",");
            for (String code : codeArray) {
                if (StringUtil.isNotBlank(code)) {
                    menuCodeSet.add(code.trim());
                }
            }
        }
        userResult.setMenuCodes(new ArrayList<>(menuCodeSet));
    }

    /**
     * 验证创建用户参数
     * @param userVO 创建用户参数
     * @return 验证结果
     */
    private ResultData<Long> validateCreateUserParam(UserVO userVO) {
        ResultData<Long> result = new ResultData<>();
        Long tenantId = SessionHolder.getCurrentTenantId();
        if (tenantId == null) {
            result.setErrorCode(ErrorCodeEnum.NOT_LOGIN);
            return result;
        }
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
    private ResultData addUserRole(UserVO userVO) {
        ResultData result = new ResultData<>();
        if (userVO.getId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        List<Long> addRoleIds = new ArrayList<>();
        List<Long> delRoleIds = new ArrayList<>();
        RoleUserQuery roleUserQuery = new RoleUserQuery();
        roleUserQuery.setTenantId(SessionHolder.getCurrentTenantId());
        roleUserQuery.setUserId(userVO.getId());
        roleUserQuery.setPage(false);
        List<RoleUserResult> existList = roleUserDao.queryRoleUserList(roleUserQuery);
        if (CollectionUtil.isEmpty(existList) && CollectionUtil.isEmpty(userVO.getRoleIds())) {
            result.setCode(ResultData.OK);
            return result;
        } else if (CollectionUtil.isNotEmpty(existList) && CollectionUtil.isNotEmpty(userVO.getRoleIds())) {
            delRoleIds = existList.stream().map(RoleUserResult::getRoleId).filter(v -> !userVO.getRoleIds().contains(v)).toList();
            List<Long> existRoleIds = existList.stream().map(RoleUserResult::getRoleId).toList();
            addRoleIds = userVO.getRoleIds().stream().filter(v -> !existRoleIds.contains(v)).toList();
        } else if (CollectionUtil.isEmpty(existList) && CollectionUtil.isNotEmpty(userVO.getRoleIds())) {
            addRoleIds = userVO.getRoleIds();
        } else if (CollectionUtil.isNotEmpty(existList) && CollectionUtil.isEmpty(userVO.getRoleIds())) {
            delRoleIds = existList.stream().map(RoleUserResult::getRoleId).toList();
        }
        Long userId = userVO.getId();
        Long createdBy = SessionHolder.getCurrentUserId();
        Long tenantId = SessionHolder.getCurrentTenantId();
        if (CollectionUtil.isNotEmpty(addRoleIds)) {
            int count = roleUserDao.batchInsertByUserId(tenantId, userId, addRoleIds, createdBy);
            if (count < 1) {
                result.setErrorCode(ErrorCodeEnum.INSERT_DATA_FAIL);
                return result;
            }
        }
        if (CollectionUtil.isNotEmpty(delRoleIds)) {
            int count = roleUserDao.deleteRoleUser(delRoleIds, userId, createdBy);
            if (count < 1) {
                result.setErrorCode(ErrorCodeEnum.DELETE_DATA_FAIL);
                return result;
            }
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
