package com.spark.manage.auth;

import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.sys.vo.RegisterVO;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-08-25 10:00:00
 * 用户注册服务接口
 */
public interface IRegisterService {

    /**
     * 用户注册
     * @param registerVO 注册参数
     * @return 注册结果
     */
    ResultData<Void> register(RegisterVO registerVO);
}
