package com.spark.bean.base;

import com.spark.enums.ErrorCodeEnum;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/3/29 14:08
 */
public class BaseException extends RuntimeException {
    /**
     * 异常状态码
     */
    private Integer code;

    public BaseException(ErrorCodeEnum errorCodeEnum) {
        super(errorCodeEnum.getDesc());
        this.code = errorCodeEnum.getValue();
    }

    public BaseException(String message, Integer code) {
        super(message);
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }
}
