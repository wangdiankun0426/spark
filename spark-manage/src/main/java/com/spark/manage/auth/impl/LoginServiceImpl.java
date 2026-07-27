package com.spark.manage.auth.impl;

import com.spark.bean.base.BaseAssert;
import com.spark.bean.log.entity.LogLogin;
import com.spark.bean.system.query.DepartmentQuery;
import com.spark.bean.system.result.DepartmentResult;
import com.spark.bean.system.vo.MessageVO;
import com.spark.bean.base.BaseException;
import com.spark.bean.system.entity.ValidateCode;
import com.spark.config.rabbitmq.MqProducer;
import com.spark.constant.ObjectCacheKey;
import com.spark.bean.system.entity.Session;
import com.spark.bean.system.query.UserQuery;
import com.spark.bean.system.result.UserResult;
import com.spark.bean.system.vo.LoginVO;
import com.spark.bean.base.ResultData;
import com.spark.bean.base.SessionHolder;
import com.spark.dao.system.DepartmentDao;
import com.spark.dao.system.RoleDao;
import com.spark.dao.system.UserDao;
import com.spark.enums.LoginTypeEnum;
import com.spark.enums.ErrorCodeEnum;
import com.spark.enums.MessageTypeEnum;
import com.spark.manage.auth.ILoginService;
import com.spark.config.redis.RedisService;
import com.spark.manage.auth.ILoginValidateService;
import com.spark.utils.*;
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
@Service
public class LoginServiceImpl implements ILoginService {
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
            this.sendLoginMessage(loginVO);
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
        UserResult userResult;
        ValidateCode code = new ValidateCode();
        code.setUuid(loginVO.getValidateId());
        code.setValue(loginVO.getValidateValue());
        ResultData<Void> validateResult = loginValidateService.checkValidateCode(code);
        BaseAssert.assertTrue(validateResult);
        if (LoginTypeEnum.PASSWORD.getValue().equals(loginVO.getLoginType())) {
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
        } else if (LoginTypeEnum.MESSAGE.getValue().equals(loginVO.getLoginType())) {
            UserQuery userQuery = new UserQuery();
            userQuery.setPhone(loginVO.getPhone());
            userResult = userDao.queryUser(userQuery);
            if (userResult == null) {
                result.setErrorCode(ErrorCodeEnum.PHONE_NOT_EXIST);
                return result;
            }
        }  else if (LoginTypeEnum.EMAIL.getValue().equals(loginVO.getLoginType())) {
            UserQuery userQuery = new UserQuery();
            userQuery.setEmail(loginVO.getEmail());
            userResult = userDao.queryUser(userQuery);
            if (userResult == null) {
                result.setErrorCode(ErrorCodeEnum.EMAIL_NOT_EXIST);
                return result;
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
     * @param loginVO 登录参数
     */
    private void sendLoginMessage(LoginVO loginVO) {
        MessageVO messageVO = new MessageVO();
        messageVO.setType(MessageTypeEnum.LOGIN.getType());
        messageVO.setTitle(MessageTypeEnum.LOGIN.getTitle());
        String content = MessageTypeEnum.LOGIN.getContent();
        messageVO.setContent(String.format(content, loginVO.getLoginName(), DateUtil.getCurrentTime(DateUtil.YYYYMMDD_HHMMSS)));
        messageVO.setUserIds(List.of(loginVO.getUserId()));
        mqProducer.sendSystemMessageMq(JsonUtil.toString(messageVO));
    }
}
