package com.spark.bean.form.result;

import com.spark.bean.base.BaseResult;
import lombok.Data;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/7/28 21:53
 */
@Data
public class FormObjValueResult extends BaseResult {
    /**
     * 对象id
     */
    private Long objId;
    /**
     * 表单id
     */
    private Long formId;
    /**
     * 类型
     */
    private String type;
    /**
     * 字段code
     */
    private String code;
    /**
     * 字段值
     */
    private String value;
    /**
     * 字段值
     */
    private String showValue;
}
