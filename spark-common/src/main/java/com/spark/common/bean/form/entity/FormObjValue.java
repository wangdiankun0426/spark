package com.spark.common.bean.form.entity;

import com.spark.common.bean.base.BaseEntity;
import lombok.Data;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/7/28 20:32
 */
@Data
public class FormObjValue extends BaseEntity {
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
