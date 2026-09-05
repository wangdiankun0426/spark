package com.spark.manage.auth;

import com.spark.common.bean.sys.entity.Session;
import com.spark.common.bean.sys.vo.LoginVO;
import com.spark.common.bean.base.ResultData;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/3/5 13:37
 */
public interface ILoginService {

    /**
     * 登录
     * @param loginVO
     * @return
     */
    ResultData<Session> login(LoginVO loginVO);

    /**
     * 登出
     * @return
     */
    ResultData<Void> logout();
}
