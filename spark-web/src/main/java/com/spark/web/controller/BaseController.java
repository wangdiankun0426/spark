package com.spark.web.controller;

import com.spark.utils.StringUtil;
import jakarta.servlet.http.HttpServletRequest;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/4/3 14:12
 */
public abstract class BaseController {

    /**
     * 从request中获取请求方IP
     * @param request 请求
     * @return 请求方IP
     */
    protected String getIpAddress(HttpServletRequest request) {
        String ip = request.getParameter("clientIp");
        if(StringUtil.isNotBlank(ip)) {
            return ip;
        }
        //如果有nginx代理 此时经过多级反向的代理，通过方法getRemoteAddr()得不到客户端真实IP，可以通过x-forwarded-for获得转发后请求信息
        ip = request.getHeader("X-cloud-ip");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("x-forwarded-for");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

}
