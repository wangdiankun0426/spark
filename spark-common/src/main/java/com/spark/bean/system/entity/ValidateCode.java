package com.spark.bean.system.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/4/7 20:38
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ValidateCode {
    /**
     * uuid
     */
    private String uuid;

    /**
     * base64图片
     */
    private String img;

    /**
     * value
     */
    private String value;

    /**
     * 登录类型
     */
    private Integer loginType;
}
