package com.spark.manage.auth.impl;

import com.spark.common.bean.base.BaseAssert;
import com.spark.common.bean.log.entity.LogLogin;
import com.spark.common.bean.sys.entity.User;
import com.spark.common.bean.sys.entity.UserProfile;
import com.spark.common.bean.sys.query.DepartmentQuery;
import com.spark.common.bean.sys.result.DepartmentResult;
import com.spark.common.bean.sys.vo.MessageVO;
import com.spark.common.bean.base.BaseException;
import com.spark.common.bean.sys.entity.ValidateCode;
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
import com.spark.dao.sys.DepartmentDao;
import com.spark.dao.sys.RoleDao;
import com.spark.dao.sys.UserDao;
import com.spark.dao.sys.UserProfileDao;
import com.spark.common.enums.LoginTypeEnum;
import com.spark.common.enums.ErrorCodeEnum;
import com.spark.common.enums.ObjectTypeEnum;
import com.spark.common.enums.StatusEnum;
import com.spark.common.enums.MessageTypeEnum;
import com.spark.manage.BaseService;
import com.spark.manage.auth.ILoginService;
import com.spark.config.redis.RedisService;
import com.spark.manage.auth.ILoginValidateService;
import com.spark.config.wecom.WeComUtil;
import com.spark.config.wechat.WeChatUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

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
public class LoginServiceImpl extends BaseService implements ILoginService {
    private final static Logger logger = LoggerFactory.getLogger(LoginServiceImpl.class);
    @Autowired
    private UserDao userDao;
    @Autowired
    private RedisService redisService;
    @Autowired
    private ILoginValidateService loginValidateService;
    @Value("${encrypt.privateKey}")
    private String privateKey;
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
    @Value("${default.user.password}")
    private String defaultUserPassword;

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
        // 验证登录
        ResultData<UserResult> validateResult = this.validateLogin(loginVO);
        if (validateResult.getCode() != ResultData.OK) {
            result.setCode(validateResult.getCode());
            result.setMessage(validateResult.getMessage());
            return result;
        }
        // 缓存用户登录信息
        UserResult userResult = validateResult.getData();
        Session session = new Session();
        boolean bo = this.cacheUserLoginInfo(userResult, session);
        if (!bo) {
            return result;
        }
        loginVO.setSessionId(session.getSessionId());
        loginVO.setUserId(userResult.getId());
        try {
            TraceLogUtil.cacheTrackUserId(loginVO.getUserId());
            TraceLogUtil.generateTrackId(UUID.randomUUID().toString().replaceAll("-",""));
            // 记录登录日志
            this.recordLoginLog(loginVO);
            // 发送登录通知
            this.sendLoginMessage(userResult);
        } finally {
            TraceLogUtil.removeTrackId();
            TraceLogUtil.removeUserId();
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
        if (loginVO == null || loginVO.getLoginType() == null) {
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
                    StringUtil.isBlank(loginVO.getLoginName());
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
     * 登录验证
     * @param loginVO 登录参数
     * @return 登录验证结果
     */
    private ResultData<UserResult> validateLogin(LoginVO loginVO) {
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
        // 处理不通登录类型
        if (LoginTypeEnum.PASSWORD.getValue().equals(loginType)) {
            UserQuery userQuery = new UserQuery();
            userQuery.setLoginName(loginVO.getLoginName());
            userResult = userDao.queryUser(userQuery);
            if (userResult == null) {
                result.setErrorCode(ErrorCodeEnum.LONG_NAME_NOT_EXIST);
                return result;
            }
            String pwd1 = userResult.getPassword();
            String pwd2 = EncryptUtil.md5(DecryptUtil.des(loginVO.getPassword(), privateKey));
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
            WeComUserRes userInfo = weComUtil.getUserInfo(loginVO.getLoginName());
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
     * 缓存用户登录信息
     * @param userResult 用户信息
     * @return  缓存结果
     */
    private boolean cacheUserLoginInfo(UserResult userResult, Session session) {
        String sessionId = UUID.randomUUID().toString().replaceAll("-","");
        session.setSessionId(sessionId);
        session.setUserId(userResult.getId());
        session.setDeptId(userResult.getDeptId());
        int dataScope = roleDao.queryUserMaxDataScope(userResult.getId());
        session.setDataScope(dataScope);
        DepartmentQuery departmentQuery = new DepartmentQuery();
        departmentQuery.setId(userResult.getDeptId());
        DepartmentResult department = departmentDao.queryDepartment(departmentQuery);
        if (department == null) {
            throw new BaseException(ErrorCodeEnum.DEPT_NOT_EXIST);
        }
        departmentQuery.setId(null);
        departmentQuery.setCode(department.getCode());
        List<DepartmentResult> departmentList = departmentDao.queryDepartmentList(departmentQuery);
        session.setDeptIds(StringUtil.join(departmentList.stream().map(DepartmentResult::getId).toList(), ","));
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
        mqProducer.sendSystemMessageMq(JsonUtil.toString(messageVO));
    }

    /**
     * 创建微信默认账号
     * @param wxSession 微信登录会话
     * @return 用户信息
     */
    private UserResult createWeChatDefaultUser(WeChatSessionRes wxSession) {
        if (wxSession == null || StringUtil.isBlank(wxSession.getOpenid())) {
            logger.warn("createWxDefaultUser skip, invalid wxSession");
            return null;
        }
        String openid = wxSession.getOpenid();
        Long userId = this.genObjectId(ObjectTypeEnum.USER);
        // 插入默认用户
        User user = new User();
        user.setId(userId);
        user.setLoginName("wx_" + openid);
        String tail = openid.length() > 4 ? openid.substring(openid.length() - 4) : openid;
        user.setName("微信用户" + tail);
        String encryptedPwd = EncryptUtil.md5(defaultUserPassword);
        user.setPassword(encryptedPwd);
        user.setDeptId(102L);
        user.setStatus(StatusEnum.NORMAL.getValue());
        user.setCreatedBy(userId);
        user.setUpdatedBy(userId);
        int count = userDao.insertDB(user);
        if (count < 1) {
            logger.error("createWxDefaultUser error, insert user fail");
            return null;
        }
        // 插入用户扩展信息并绑定微信openid
        UserProfile wxProfile = new UserProfile();
        wxProfile.setId(userId);
        wxProfile.setWxOpenId(openid);
        wxProfile.setWxUnionId(wxSession.getUnionid());
        wxProfile.setCreatedBy(userId);
        wxProfile.setUpdatedBy(userId);
        try {
            userProfileDao.insertDB(wxProfile);
        } catch (Exception e) {
            // 并发首登触发唯一索引冲突时回查已绑定账号
            logger.error("createWxDefaultUser error, insert profile fail", e);
            UserProfile existProfile = userProfileDao.queryByWxOpenId(openid);
            if (existProfile != null) {
                UserQuery userQuery = new UserQuery();
                userQuery.setId(existProfile.getId());
                return userDao.queryUser(userQuery);
            }
            return null;
        }
        UserQuery userQuery = new UserQuery();
        userQuery.setId(userId);
        return userDao.queryUser(userQuery);
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
