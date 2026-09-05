package com.spark.common.bean.base;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.spark.common.enums.ErrorCodeEnum;
import lombok.Data;

import java.io.Serializable;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/1/25 17:51
 * .............................................
 * .............................................
 * 佛祖保佑             永无BUG
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResultData<T> implements Serializable {

    public final static int OK = 200;
    /**
     * 状态码
     */
    private int code = 500;

    /**
     * 状态信息
     */
    private String message;

    /**
     * 返回数据
     */
    private T data;

    /**
     * 操作对象id
     * 用户记录操作日志使用
     */
    private Long objId;

    public ResultData() {

    }

    public ResultData(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public void setErrorCode(ErrorCodeEnum errorCode) {
        if(errorCode != null) {
            this.setCode(errorCode.getValue());
            this.setMessage(errorCode.getDesc());
        }
    }

}
