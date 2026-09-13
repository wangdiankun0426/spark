package com.spark.manage.auth.impl;

import com.spark.common.bean.base.BaseAssert;
import com.spark.common.bean.log.entity.LogLogin;
import com.spark.common.bean.sys.entity.User;
import com.spark.common.bean.sys.entity.UserProfile;
import com.spark.common.bean.sys.query.DepartmentQuery;
import com.spark.common.bean.sys.query.TenantQuery;
import com.spark.common.bean.sys.query.TenantUserQuery;
import com.spark.common.bean.sys.result.DepartmentResult;
import com.spark.common.bean.sys.result.TenantResult;
import com.spark.common.bean.sys.result.TenantUserResult;
import com.spark.common.bean.sys.vo.MessageVO;
import com.spark.common.bean.sys.entity.ValidateCode;
import com.spark.common.bean.sys.vo.UserVO;
import com.spark.common.enums.*;
import com.spark.common.utils.*;
import com.spark.config.aspectj.annotation.LogPrint;
import com.spark.config.rabbitmq.MqProducer;
import com.spark.config.wecom.response.WeComUserRes;
import com.spark.config.wechat.response.WeChatSessionRes;
import com.spark.common.constant.ObjectCacheKey;
import com.spark.common.bean.sys.entity.Session;
import com.spark.common.bean.sys.query.UserQuery;
import com.spark.common.bean.sys.result.UserResult;
import com.spark.common.bean.sys.vo.LoginVO;
import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.base.SessionHolder;
import com.spark.dao.sys.*;
import com.spark.manage.auth.ILoginService;
import com.spark.config.redis.RedisService;
import com.spark.manage.auth.ILoginValidateService;
import com.spark.manage.auth.IEncryptKeyService;
import com.spark.config.wecom.WeComUtil;
import com.spark.config.wechat.WeChatUtil;
import com.spark.manage.sys.ITenantConfigService;
import com.spark.manage.sys.IUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/3/5 13:37
 */
@LogPrint
@Service
public class LoginServiceImpl implements ILoginService {
    private final static Logger logger = LoggerFactory.getLogger(LoginServiceImpl.class);
    @Autowired
    private UserDao userDao;
    @Autowired
    private RedisService redisService;
    @Autowired
    private ILoginValidateService loginValidateService;
    @Autowired
    private IEncryptKeyService encryptKeyService;
    @Autowired
    private MqProducer mqProducer;
    @Autowired
    private RoleDao roleDao;
    @Autowired
    private DepartmentDao departmentDao;
    @Autowired
    private WeComUtil weComUtil;
    @Autowired
    private UserProfileDao userProfileDao;
    @Autowired
    private WeChatUtil weChatUtil;
    @Autowired
    private TenantUserDao tenantUserDao;
    @Autowired
    private TenantDao tenantDao;
    @Autowired
    private IUserService userService;
    @Autowired
    private ITenantConfigService tenantConfigService;

    /**
     * 登录
     * @param loginVO 登录参数
     * @return 登录结果
     */
    @Override
    public ResultData<Session> login(LoginVO loginVO) {
        ResultData<Session> result = new ResultData<>();
        // 检查登录类型参数
        ResultData<Void> checkResult = this.checkLoginTypeParam(loginVO);
        if (checkResult.getCode() != ResultData.OK) {
            result.setCode(checkResult.getCode());
            result.setMessage(checkResult.getMessage());
            return result;
        }
        // 验证登录类型
        ResultData<UserResult> validateLoginTypeResult = this.validateLoginType(loginVO);
        if (validateLoginTypeResult.getCode() != ResultData.OK) {
            result.setCode(validateLoginTypeResult.getCode());
            result.setMessage(validateLoginTypeResult.getMessage());
            return result;
        }
        UserResult userResult = validateLoginTypeResult.getData();
        // 验证用户租户
        ResultData<Void> validateUserTenantData = this.validateUserTenant(userResult);
        if (validateUserTenantData.getCode() != ResultData.OK) {
            result.setCode(validateUserTenantData.getCode());
            result.setMessage(validateUserTenantData.getMessage());
            return result;
        }
        // 缓存用户登录信息
        Session session = new Session();
        boolean bo = this.cacheUserLoginInfo(userResult, session, loginVO.getLoginPlatform());
        if (!bo) {
            return result;
        }
        loginVO.setSessionId(session.getSessionId());
        loginVO.setUserId(userResult.getId());
        loginVO.setTenantId(userResult.getCurrentTenantId());
        try {
            TraceLogUtil.cacheTrackUserId(loginVO.getUserId());
            TraceLogUtil.cacheTenantId(loginVO.getTenantId());
            TraceLogUtil.cacheLoginPlatform(LoginPlatformEnum.indexOf(loginVO.getLoginPlatform()).getDesc());
            TraceLogUtil.generateTrackId(UUID.randomUUID().toString().replaceAll("-",""));
            // 记录登录日志
            this.recordLoginLog(loginVO);
            // 发送登录通知
            this.sendLoginMessage(userResult);
        } finally {
            TraceLogUtil.removeTrackId();
            TraceLogUtil.removeUserId();
            TraceLogUtil.removeTenantId();
            TraceLogUtil.removeLoginPlatform();
        }
        result.setData(session);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 登出
     * @return 登出结果
     */
    @Override
    public ResultData<Void> logout() {
        ResultData<Void> result = new ResultData<>();
        String sessionId = SessionHolder.getCurrentSessionId();
        if (StringUtil.isBlank(sessionId)) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        String sessionIdKey = ObjectCacheKey.LOGIN_SESSION + sessionId;
        boolean bo = redisService.del(sessionIdKey);
        if (!bo) {
            return result;
        }
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
     * 检查登录类型参数
     * @param loginVO 登录参数
     * @return 检查结果
     *
     * Predicate<LoginVO> predicate 是一个函数式接口，用于对 LoginVO 对象进行条件判断，返回布尔值。
     */
    private ResultData<Void> checkLoginTypeParam(LoginVO loginVO) {
        ResultData<Void> result = new ResultData<>();
        if (loginVO == null || loginVO.getLoginType() == null || loginVO.getLoginPlatform() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        Predicate<LoginVO> checkPredicate = vo -> false;
        if (LoginTypeEnum.PASSWORD.getValue().equals(loginVO.getLoginType())) {
            checkPredicate = vo ->
                    StringUtil.isBlank(loginVO.getLoginName())
                            || StringUtil.isBlank(loginVO.getPassword())
                            || StringUtil.isBlank(loginVO.getValidateId())
                            || StringUtil.isBlank(loginVO.getValidateValue());
        } else if (LoginTypeEnum.MESSAGE.getValue().equals(loginVO.getLoginType())) {
            checkPredicate = vo ->
                    StringUtil.isBlank(loginVO.getPhone())
                            || StringUtil.isBlank(loginVO.getValidateId())
                            || StringUtil.isBlank(loginVO.getValidateValue());
        } else if (LoginTypeEnum.EMAIL.getValue().equals(loginVO.getLoginType())) {
            checkPredicate = vo ->
                    StringUtil.isBlank(loginVO.getEmail())
                            || StringUtil.isBlank(loginVO.getValidateId())
                            || StringUtil.isBlank(loginVO.getValidateValue());
        } else if (LoginTypeEnum.WECOM_OAUTH.getValue().equals(loginVO.getLoginType())) {
            checkPredicate = vo ->
                    StringUtil.isBlank(loginVO.getLoginName())
                            || loginVO.getTenantId() == null ;
        } else if (LoginTypeEnum.WECHAT.getValue().equals(loginVO.getLoginType())) {
            checkPredicate = vo ->
                    StringUtil.isBlank(loginVO.getWxCode());
        }
        boolean bo = checkPredicate.test(loginVO);
        if (bo) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 验证登录类型
     * @param loginVO 登录参数
     * @return 登录验证结果
     */
    private ResultData<UserResult> validateLoginType(LoginVO loginVO) {
        ResultData<UserResult> result = new ResultData<>();
        UserResult userResult = null;
        Integer loginType = loginVO.getLoginType();
        // 校验验证码
        ValidateCode code = new ValidateCode();
        code.setUuid(loginVO.getValidateId());
        code.setValue(loginVO.getValidateValue());
        code.setLoginType(loginType);
        ResultData<Void> validateResult = loginValidateService.checkValidateCode(code);
        BaseAssert.assertTrue(validateResult);
        // 处理不同登录类型
        if (LoginTypeEnum.PASSWORD.getValue().equals(loginType)) {
            UserQuery userQuery = new UserQuery();
            userQuery.setLoginName(loginVO.getLoginName());
            userResult = userDao.queryUser(userQuery);
            if (userResult == null) {
                result.setErrorCode(ErrorCodeEnum.LONG_NAME_NOT_EXIST);
                return result;
            }
            // 一次性消费密钥ID对应的密钥
            String desKey = encryptKeyService.consumeEncryptKey(loginVO.getKeyId());
            if (StringUtil.isBlank(desKey)) {
                result.setErrorCode(ErrorCodeEnum.ENCRYPT_KEY_INVALID);
                return result;
            }
            String password = DecryptUtil.des(loginVO.getPassword(), desKey);
            if (StringUtil.isBlank(password)) {
                result.setErrorCode(ErrorCodeEnum.ENCRYPT_KEY_INVALID);
                return result;
            }
            String pwd1 = userResult.getPassword();
            String pwd2 = EncryptUtil.md5(password);
            if (!pwd1.equals(pwd2)) {
                result.setErrorCode(ErrorCodeEnum.LONG_PASSWORD_ERROR);
                return result;
            }
        } else if (LoginTypeEnum.MESSAGE.getValue().equals(loginType)) {
            UserQuery userQuery = new UserQuery();
            userQuery.setPhone(loginVO.getPhone());
            userResult = userDao.queryUser(userQuery);
            if (userResult == null) {
                result.setErrorCode(ErrorCodeEnum.PHONE_NOT_EXIST);
                return result;
            }
        }  else if (LoginTypeEnum.EMAIL.getValue().equals(loginType)) {
            UserQuery userQuery = new UserQuery();
            userQuery.setEmail(loginVO.getEmail());
            userResult = userDao.queryUser(userQuery);
            if (userResult == null) {
                result.setErrorCode(ErrorCodeEnum.EMAIL_NOT_EXIST);
                return result;
            }
        } else if (LoginTypeEnum.WECOM_OAUTH.getValue().equals(loginType)) {
            ResultData<Map<String, String>> mapResult = tenantConfigService.queryTenantConfigMap(loginVO.getTenantId());
            Map<String, String> configMap = mapResult.getData();
            String corpId = configMap.get(TenantConfigEnum.WECOM_CORP_ID.getKey());
            String corpSecret = configMap.get(TenantConfigEnum.WECOM_CORP_SECRET.getKey());
            WeComUserRes userInfo = weComUtil.getUserInfo(loginVO.getLoginName(), corpId, corpSecret);
            if (userInfo == null || StringUtil.isBlank(userInfo.getUserid())) {
                result.setErrorCode(ErrorCodeEnum.WECOM_LOGIN_FAIL);
                return result;
            }
            UserProfile userProfile = userProfileDao.queryByWecomId(userInfo.getUserid());
            if (userProfile == null) {
                result.setErrorCode(ErrorCodeEnum.WECOM_NOT_BIND_USER);
                return result;
            }
            UserQuery userQuery = new UserQuery();
            userQuery.setId(userProfile.getId());
            userResult = userDao.queryUser(userQuery);
            if (userResult == null) {
                result.setErrorCode(ErrorCodeEnum.EMAIL_NOT_EXIST);
                return result;
            }
        } else if (LoginTypeEnum.WECHAT.getValue().equals(loginType)) {
            WeChatSessionRes weChatSessionRes = weChatUtil.code2Session(loginVO.getWxCode());
            if (weChatSessionRes == null || StringUtil.isBlank(weChatSessionRes.getOpenid())) {
                result.setErrorCode(ErrorCodeEnum.WECHAT_LOGIN_FAIL);
                return result;
            }
            UserProfile wxProfile = userProfileDao.queryByWxOpenId(weChatSessionRes.getOpenid());
            if (wxProfile != null) {
                UserQuery wxUserQuery = new UserQuery();
                wxUserQuery.setId(wxProfile.getId());
                userResult = userDao.queryUser(wxUserQuery);
            }
            if (userResult == null) {
                // 未绑定则自动创建默认账号
                userResult = this.createWeChatDefaultUser(weChatSessionRes);
                if (userResult == null) {
                    result.setErrorCode(ErrorCodeEnum.WECHAT_LOGIN_FAIL);
                    return result;
                }
            }
        } else {
            result.setErrorCode(ErrorCodeEnum.LOGIN_TYPE_UNKNOWN);
            return result;
        }
        result.setData(userResult);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 验证用户租户
     * @param userResult
     * @return
     */
    private ResultData<Void> validateUserTenant(UserResult userResult) {
        ResultData<Void> result = new ResultData<>();
        if (Objects.equals(AccountTypeEnum.indexOf(userResult.getAccountType()).getValue(), AccountTypeEnum.SITE_ADMIN.getValue())) {
            result.setCode(ResultData.OK);
            return result;
        }
        Long userId = userResult.getId();
        TenantUserQuery tenantUserQuery = new TenantUserQuery();
        tenantUserQuery.setUserId(userId);
        tenantUserQuery.setPage(false);
        List<TenantUserResult> tenantUserList = tenantUserDao.queryTenantUserList(tenantUserQuery);
        if (CollectionUtil.isEmpty(tenantUserList)) {
            result.setErrorCode(ErrorCodeEnum.USER_NOT_JOIN_NORMAL_TENANT);
            return result;
        }
        List<Long> tenantIds = tenantUserList.stream().map(TenantUserResult::getTenantId).distinct().toList();
        TenantQuery tenantQuery = new TenantQuery();
        tenantQuery.setIds(tenantIds);
        tenantQuery.setStatus(StatusEnum.NORMAL.getValue());
        tenantQuery.setPage(false);
        List<TenantResult> tenantList = tenantDao.queryTenantList(tenantQuery);
        if (CollectionUtil.isEmpty(tenantList)) {
            result.setErrorCode(ErrorCodeEnum.USER_NOT_JOIN_NORMAL_TENANT);
            return result;
        }
        List<Long> finalTenantIds = tenantList.stream().map(TenantResult::getId).distinct().toList();
        Map<Long, TenantUserResult> tenantUserMap = tenantUserList.stream().filter(v -> finalTenantIds.contains(v.getTenantId())).collect(Collectors.toMap(TenantUserResult::getTenantId, v -> v, (v1, v2) -> v2));
        TenantUserResult tenantUserResult = tenantUserMap.get(userResult.getCurrentTenantId());
        if (tenantUserResult == null) {
            tenantUserResult = tenantUserMap.get(finalTenantIds.get(0));
            // 更新用户的当前租户id
            User user = new User();
            user.setId(userId);
            user.setCurrentTenantId(tenantUserResult.getTenantId());
            userDao.updateDBById(user);
        }
        userResult.setRoleType(tenantUserResult.getRoleType());
        userResult.setDeptId(tenantUserResult.getDeptId());
        userResult.setCurrentTenantId(tenantUserResult.getTenantId());
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 缓存用户登录信息
     * @param userResult 用户信息
     * @param session 登录会话
     * @param loginPlatform 登录平台
     * @return  缓存结果
     */
    private boolean cacheUserLoginInfo(UserResult userResult, Session session, Integer loginPlatform) {
        String sessionId = UUID.randomUUID().toString().replaceAll("-","");
        session.setSessionId(sessionId);
        Long tenantId = userResult.getCurrentTenantId();
        session.setTenantId(tenantId);
        session.setUserId(userResult.getId());
        session.setDeptId(userResult.getDeptId());
        session.setAccountType(userResult.getAccountType());
        session.setLoginPlatform(loginPlatform);
        session.setRoleType(userResult.getRoleType());
        int dataScope = roleDao.queryUserMaxDataScope(tenantId, userResult.getId());
        session.setDataScope(dataScope);
        DepartmentQuery departmentQuery = new DepartmentQuery();
        departmentQuery.setTenantId(tenantId);
        departmentQuery.setId(userResult.getDeptId());
        DepartmentResult department = departmentDao.queryDepartment(departmentQuery);
        if (department != null) {
            departmentQuery.setId(null);
            departmentQuery.setCode(department.getCode());
            List<DepartmentResult> departmentList = departmentDao.queryDepartmentList(departmentQuery);
            session.setDeptIds(StringUtil.join(departmentList.stream().map(DepartmentResult::getId).toList(), ","));
        }
        String sessionIdKey = ObjectCacheKey.LOGIN_SESSION + sessionId;
        return redisService.setStr(sessionIdKey, JsonUtil.toString(session), 60*60);
    }

    /**
     * 记录登录日志
     * @param loginVO 登录参数
     */
    private void recordLoginLog(LoginVO loginVO) {
        LogLogin logLogin = new LogLogin();
        logLogin.setIpaddress(loginVO.getIpaddress());
        logLogin.setSessionId(loginVO.getSessionId());
        logLogin.setLoginPlatform(loginVO.getLoginPlatform());
        logLogin.setLoginType(loginVO.getLoginType());
        logLogin.setCreatedBy(loginVO.getUserId());
        logLogin.setUpdatedBy(loginVO.getUserId());
        logLogin.setTenantId(loginVO.getTenantId());
        mqProducer.sendLoginLogMq(JsonUtil.toString(logLogin));
    }

    /**
     * 发送登录消息
     * @param userResult 登录参数
     */
    private void sendLoginMessage(UserResult userResult) {
        MessageVO messageVO = new MessageVO();
        messageVO.setType(MessageTypeEnum.LOGIN.getType());
        messageVO.setTitle(MessageTypeEnum.LOGIN.getTitle());
        String content = MessageTypeEnum.LOGIN.getContent();
        messageVO.setContent(String.format(content, userResult.getName(), DateUtil.getCurrentTime(DateUtil.YYYYMMDD_HHMMSS)));
        messageVO.setUserIds(List.of(userResult.getId()));
        messageVO.setRefId(userResult.getId());
        messageVO.setTenantId(userResult.getCurrentTenantId());
        mqProducer.sendSystemMessageMq(JsonUtil.toString(messageVO));
    }

    /**
     * 创建微信默认账号
     * @param wxSession 微信登录会话
     * @return 用户信息
     */
    private UserResult createWeChatDefaultUser(WeChatSessionRes wxSession) {
        if (wxSession == null || StringUtil.isBlank(wxSession.getOpenid())) {
            logger.warn("invalid wxSession");
            return null;
        }
        String openid = wxSession.getOpenid();
        UserProfile existProfile = userProfileDao.queryByWxOpenId(openid);
        if (existProfile != null) {
            UserQuery userQuery = new UserQuery();
            userQuery.setId(existProfile.getId());
            return userDao.queryUser(userQuery);
        }
        try {
            SessionHolder.setCurrentUserId(101L);
            SessionHolder.setCurrentTenantId(103L);
            UserVO userVO = new UserVO();
            userVO.setLoginName("wx_" + openid);
            String tail = openid.length() > 4 ? openid.substring(openid.length() - 4) : openid;
            userVO.setName("微信用户" + tail);
            userVO.setWxOpenId(openid);
            userVO.setWxUnionId(wxSession.getUnionid());
            ResultData<Long> result = userService.createUser(userVO);
            BaseAssert.assertTrue(result);
            UserQuery userQuery = new UserQuery();
            userQuery.setId(result.getData());
            return userDao.queryUser(userQuery);
        } catch (Exception e) {
            logger.error("createWxDefaultUser error", e);
        } finally {
            SessionHolder.clearLocalSession();
        }
        return null;
    }
}
