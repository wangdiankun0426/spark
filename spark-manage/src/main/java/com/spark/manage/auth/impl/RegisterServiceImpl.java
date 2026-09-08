package com.spark.manage.auth.impl;

import com.spark.common.bean.base.BaseAssert;
import com.spark.common.bean.base.SessionHolder;
import com.spark.common.bean.sys.entity.User;
import com.spark.common.bean.sys.entity.UserProfile;
import com.spark.common.bean.sys.entity.ValidateCode;
import com.spark.common.bean.sys.query.UserQuery;
import com.spark.common.bean.sys.result.UserResult;
import com.spark.common.bean.sys.vo.RegisterVO;
import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.sys.vo.TenantUserVO;
import com.spark.common.enums.*;
import com.spark.dao.sys.UserDao;
import com.spark.dao.sys.UserProfileDao;
import com.spark.manage.BaseService;
import com.spark.manage.auth.ILoginValidateService;
import com.spark.manage.auth.IRegisterService;
import com.spark.common.utils.DecryptUtil;
import com.spark.common.utils.EncryptUtil;
import com.spark.common.utils.StringUtil;
import com.spark.manage.sys.ITenantUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-08-25 10:00:00
 * 用户注册服务实现
 */
@Service
public class RegisterServiceImpl extends BaseService implements IRegisterService {
    private final static Logger logger = LoggerFactory.getLogger(RegisterServiceImpl.class);
    @Autowired
    private UserDao userDao;
    @Autowired
    private UserProfileDao userProfileDao;
    @Autowired
    private ILoginValidateService loginValidateService;
    @Autowired
    private ITenantUserService tenantUserService;
    @Value("${encrypt.privateKey}")
    private String privateKey;

    /**
     * 用户注册
     * @param registerVO 注册参数
     * @return 注册结果
     */
    @Override
    public ResultData<Void> register(RegisterVO registerVO) {
        ResultData<Void> result = new ResultData<>();
        // 校验参数
        result = this.validateRegisterParam(registerVO);
        if (result.getCode() != ResultData.OK) {
            return result;
        }
        // 校验验证码
        ValidateCode code = new ValidateCode();
        code.setUuid(registerVO.getValidateId());
        code.setValue(registerVO.getValidateValue());
        code.setLoginType(LoginTypeEnum.PASSWORD.getValue());
        ResultData<Void> validateResult = loginValidateService.checkValidateCode(code);
        BaseAssert.assertTrue(validateResult);
        // 校验登录名是否已存在
        UserQuery userQuery = new UserQuery();
        userQuery.setLoginName(registerVO.getLoginName());
        UserResult existingUser = userDao.queryUser(userQuery);
        if (existingUser != null) {
            result.setErrorCode(ErrorCodeEnum.USER_SAME_LOGIN_NAME_EXIST);
            return result;
        }
        // 生成用户ID
        Long userId = this.genObjectId(ObjectTypeEnum.USER);
        // 创建用户
        User user = new User();
        user.setId(userId);
        user.setLoginName(registerVO.getLoginName());
        user.setName(registerVO.getLoginName());
        user.setPassword(EncryptUtil.md5(DecryptUtil.des(registerVO.getPassword(), privateKey)));
        user.setSex(registerVO.getSex());
        user.setStatus(StatusEnum.NORMAL.getValue());
        user.setAccountType(AccountTypeEnum.COMMON.getValue());
        user.setCreatedBy(userId);
        user.setUpdatedBy(userId);
        user.setCurrentTenantId(103L);
        int count = userDao.insertDB(user);
        if (count < 1) {
            logger.error("register error, insert user fail");
            result.setErrorCode(ErrorCodeEnum.INSERT_DATA_FAIL);
            return result;
        }
        // 创建用户扩展信息
        UserProfile userProfile = new UserProfile();
        userProfile.setId(userId);
        userProfile.setCreatedBy(userId);
        userProfile.setUpdatedBy(userId);
        count = userProfileDao.insertDB(userProfile);
        if (count < 1) {
            result.setErrorCode(ErrorCodeEnum.INSERT_DATA_FAIL);
            return result;
        }
        // 加入租户
        try {
            SessionHolder.setCurrentUserId(userId);
            TenantUserVO tenantUserVO = new TenantUserVO();
            tenantUserVO.setTenantId(103L);
            tenantUserVO.setUserIds(List.of(userId));
            result = tenantUserService.addUser(tenantUserVO);
            BaseAssert.assertTrue(result);
        } finally {
            SessionHolder.clearLocalSession();
        }
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 校验注册参数
     * @param registerVO 注册参数
     * @return 校验结果
     */
    private ResultData<Void> validateRegisterParam(RegisterVO registerVO) {
        ResultData<Void> result = new ResultData<>();
        if (registerVO == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        if (StringUtil.isBlank(registerVO.getLoginName())) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        if (StringUtil.isBlank(registerVO.getPassword())) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        if (StringUtil.isBlank(registerVO.getValidateId())) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        if (StringUtil.isBlank(registerVO.getValidateValue())) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 查询最大id
     * @return 最大id
     */
    @Override
    protected Long queryMaxId() {
        return userDao.queryUserMaxId();
    }
}
