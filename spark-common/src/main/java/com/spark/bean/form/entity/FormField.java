package com.spark.bean.form.entity;

import com.spark.bean.base.BaseEntity;
import lombok.Data;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/7/3 16:48
 */
@Data
public class FormField extends BaseEntity {

    /**
     * 表单id
     */
    private Long formId;

    /**
     * 版本id
     */
    private Long revId;

    /**
     * 字段标题
     */
    private String label;

    /**
     * 字段名
     */
    private String code;

    /**
     * 字段类型
     */
    private String type;
}
