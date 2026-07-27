package com.spark.bean.form.result;

import com.spark.bean.base.BaseEntity;
import com.spark.bean.base.BaseResult;
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
public class FormFieldResult extends BaseResult {

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
