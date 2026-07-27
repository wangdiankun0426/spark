package com.spark.manage.auth;

import com.spark.bean.system.vo.LoginVO;
import com.spark.bean.base.ResultData;
import com.spark.bean.system.entity.ValidateCode;

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
     * 构造短息验证码
     * @param loginVO
     * @return
     */
    ResultData<ValidateCode> generateMessageCode(LoginVO loginVO);

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
