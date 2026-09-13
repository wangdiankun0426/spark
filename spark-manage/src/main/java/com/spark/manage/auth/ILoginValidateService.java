package com.spark.manage.auth;

import com.spark.common.bean.sys.vo.LoginVO;
import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.sys.entity.ValidateCode;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/4/7 20:30
 */
public interface ILoginValidateService {

    /**
     * 构造验证码
     * @return
     */
    ResultData<ValidateCode> generateValidateCode();

    /**
     * 构造短信验证码
     * @param loginVO
     * @return
     */
    ResultData<ValidateCode> generateSmsCode(LoginVO loginVO);

    /**
     * 获取邮箱验证码
     * @param loginVO
     * @return
     */
    ResultData<ValidateCode> generateEmailCode(LoginVO loginVO);

    /**
     * 校验验证码
     * @param code
     * @return
     */
    ResultData<Void> checkValidateCode(ValidateCode code);

}
