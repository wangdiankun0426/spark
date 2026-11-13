package com.spark.web.rest.exception;

import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-11-13 14:04:29
 * 全局参数绑定保护，禁止请求绑定服务端专用字段
 */
@ControllerAdvice
public class BindingProtectionAdvice {

    /**
     * 禁用数据权限片段的请求绑定
     * @param binder 数据绑定器
     */
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setDisallowedFields("dataScopeSQL");
    }
}
