package com.spark.common.bean.base;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2025/11/16 18:05
 */
public class BaseAssert {

    /**
     * 断言成功
     * @param data 结果
     */
    public static void assertTrue(ResultData<?> data) {
        if (data.getCode() == ResultData.OK) {
            return;
        }
        throw new BaseException(data.getMessage(), data.getCode());
    }
}
