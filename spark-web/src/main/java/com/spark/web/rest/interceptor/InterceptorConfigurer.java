package com.spark.web.rest.interceptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/3/4 22:17
 * 添加拦截器
 * HandlerInterceptor的局限性：
 * 拦截器 (HandlerInterceptor) 是在 Spring MVC 的 DispatcherServlet 内部工作的。
 * 它在请求被路由到具体的 Controller 方法之后才会被调用（preHandle 是在 HandlerExecutionChain 执行前）。
 * 所以适用于 检查登录、限制非法源访问等
 */
@Configuration
public class InterceptorConfigurer implements WebMvcConfigurer {
    @Autowired
    private LoginInterceptor loginInterceptor;
    @Autowired
    private CorsInterceptor corsInterceptor;

    /**
     * 添加拦截器
     * @param registry 注册拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 配置跨域资源共享拦截器
        registry.addInterceptor(corsInterceptor)
                // 拦截的地址
                .addPathPatterns("/**")
                // 不拦截的地址
                .excludePathPatterns("/error");
        // 配置登录验证拦截器
        ArrayList<String> excludePatterns = new ArrayList<>();
        excludePatterns.add("/");
        excludePatterns.add("/auth/login");
        excludePatterns.add("/error");
        excludePatterns.add("/sys/user/avatar");
        excludePatterns.add("/auth/validateCode");
        excludePatterns.add("/auth/smsCode");
        excludePatterns.add("/auth/emailCode");
        excludePatterns.add("/auth/register");
        excludePatterns.add("/auth/encryptKey");
        excludePatterns.add("/weCom/**");
        registry.addInterceptor(loginInterceptor)
                // 拦截的地址
                .addPathPatterns("/**")
                // 不拦截的地址
                .excludePathPatterns(excludePatterns);
    }
}
