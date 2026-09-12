package com.spark.manage.auth.impl;

import com.spark.common.bean.base.BaseAssert;
import com.spark.common.bean.base.SessionHolder;
import com.spark.common.bean.sys.entity.ValidateCode;
import com.spark.common.bean.sys.vo.RegisterVO;
import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.sys.vo.UserVO;
import com.spark.common.enums.*;
import com.spark.manage.auth.ILoginValidateService;
import com.spark.manage.auth.IRegisterService;
import com.spark.common.utils.DecryptUtil;
import com.spark.common.utils.EncryptUtil;
import com.spark.common.utils.StringUtil;
import com.spark.manage.sys.IUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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
public class RegisterServiceImpl implements IRegisterService {
    private final static Logger logger = LoggerFactory.getLogger(RegisterServiceImpl.class);
    @Autowired
    private ILoginValidateService loginValidateService;
    @Autowired
    private IUserService userService;
    @Value("${encrypt.privateKey}")
    private String privateKey;

    /**
     * 用户注册
     * @param registerVO 注册参数
     * @return 注册结果
     */
    @Override
    public ResultData<Void> register(RegisterVO registerVO) {
        ResultData<Void> result = this.validateRegisterParam(registerVO);
        BaseAssert.assertTrue(result);
        // 校验验证码
        ValidateCode code = new ValidateCode();
        code.setUuid(registerVO.getValidateId());
        code.setValue(registerVO.getValidateValue());
        code.setLoginType(LoginTypeEnum.PASSWORD.getValue());
        result = loginValidateService.checkValidateCode(code);
        BaseAssert.assertTrue(result);
        try {
            SessionHolder.setCurrentUserId(101L);
            SessionHolder.setCurrentTenantId(103L);
            UserVO userVO = new UserVO();
            userVO.setLoginName(registerVO.getLoginName());
            userVO.setName(registerVO.getLoginName());
            userVO.setPassword(EncryptUtil.md5(DecryptUtil.des(registerVO.getPassword(), privateKey)));
            userVO.setSex(registerVO.getSex());
            ResultData<Long> cuResult = userService.createUser(userVO);
            BaseAssert.assertTrue(cuResult);
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
}
