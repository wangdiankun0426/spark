package com.spark.web.rest.controller.manage.auth;

import com.spark.common.bean.base.SessionHolder;
import com.spark.common.bean.sys.entity.Session;
import com.spark.common.bean.sys.query.UserQuery;
import com.spark.common.bean.sys.result.UserResult;
import com.spark.common.bean.sys.vo.LoginVO;
import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.sys.entity.ValidateCode;
import com.spark.manage.sys.IUserService;
import com.spark.web.rest.controller.BaseController;
import com.spark.manage.auth.ILoginValidateService;
import com.spark.manage.auth.ILoginService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
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
 * @since 2024/3/4 22:29
 */
@RestController
@RequestMapping("auth")
public class LoginController extends BaseController {
    @Autowired
    private ILoginService loginService;
    @Autowired
    private ILoginValidateService loginValidateService;
    @Autowired
    private IUserService userService;

    /**
     * 获取验证码
     * @return 验证码
     */
    @GetMapping("validateCode")
    public ResultData<ValidateCode> queryValidateCode() {
        return loginValidateService.generateValidateCode();
    }

    /**
     * 获取短信验证码
     * @return 验证码
     */
    @GetMapping("messageCode")
    public ResultData<ValidateCode> queryMessageCode(LoginVO loginVO) {
        return loginValidateService.generateMessageCode(loginVO);
    }

    /**
     * 获取邮箱验证码
     * @return 验证码
     */
    @GetMapping("emailCode")
    public ResultData<ValidateCode> queryEmailCode(LoginVO loginVO) {
        return loginValidateService.generateEmailCode(loginVO);
    }

    /**
     * 登录
     *
     * @param request 请求对象
     * @param loginVO 登录参数
     * @return 登录结果
     */
    @PostMapping("login")
    public ResultData<Session> login(HttpServletRequest request, LoginVO loginVO) {
        loginVO.setIpaddress(super.getIpAddress(request));
        return loginService.login(loginVO);
    }

    /**
     * 登出
     * @return 登出结果
     */
    @PostMapping("logout")
    public ResultData<Void> logout() {
        return loginService.logout();
    }

    /**
     * 强制退出
     * @param loginVO 退出的参数
     * @return 退出结果
     */
    @PostMapping("forceLogout")
    public ResultData<Void> forceLogout(LoginVO loginVO) {
        return loginService.forceLogout(loginVO);
    }

    /**
     * 查询用户session
     * @return 验证码
     */
    @GetMapping("session")
    public ResultData<UserResult> session() {
        UserQuery query = new UserQuery();
        query.setId(SessionHolder.getCurrentUserId());
        return userService.queryUserDetail(query);
    }
}
