package com.spark.web.interceptor;

import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/6/2 21:37
 * 跨域资源共享拦截器
 * 这不是为了解决浏览器端的跨域问题的，而是设置允许访问的请求的源。
 */
@Component
public class CorsInterceptor implements HandlerInterceptor {
    private final static Logger logger = LoggerFactory.getLogger(CorsInterceptor.class);

    /**
     * 重写预处理方法
     *
     * @param request 请求
     * @param response 响应
     * @param handler 处理器
     * @return 拦截结果
     */
    @Override
    public boolean preHandle(HttpServletRequest request, @Nullable HttpServletResponse response, @Nullable Object handler) {
        String requestUrl = request.getRequestURI();
        String origin = request.getHeader(HttpHeaders.ORIGIN);
        if (origin == null) {
            origin = "http://127.0.0.1:81";
        }
        if (response != null) {
            response.setHeader("Access-Control-Allow-Origin", origin);
        }
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            if (response != null) {
                response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE");
                response.setHeader("Access-Control-Allow-Headers", "Authorization, Content-Type");
                response.setHeader("Access-Control-Max-Age", "3600");
                response.setHeader("Access-Control-Allow-Credentials", "true");
                response.setStatus(javax.servlet.http.HttpServletResponse.SC_OK);
            }
            return false;
        } else {
            if (response != null) {
                response.setHeader("Access-Control-Allow-Headers", "Authorization, Content-Type");
                response.setHeader("Access-Control-Max-Age", "3600");
                response.setHeader("Access-Control-Allow-Credentials", "true");
            }
        }
        return true;
    }
}
