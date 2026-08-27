package com.spark.web.controller.auth;

import com.spark.bean.system.vo.RegisterVO;
import com.spark.bean.base.ResultData;
import com.spark.manage.auth.IRegisterService;
import com.spark.web.controller.BaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-08-25 10:00:00
 * 用户注册控制器
 */
@RestController
@RequestMapping("auth")
public class RegisterController extends BaseController {
    @Autowired
    private IRegisterService registerService;

    /**
     * 用户注册
     * @param registerVO 注册参数
     * @return 注册结果
     */
    @PostMapping("register")
    private ResultData<Void> register(RegisterVO registerVO) {
        return registerService.register(registerVO);
    }
}
