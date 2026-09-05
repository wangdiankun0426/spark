package com.spark.common.bean.form.query;

import com.spark.common.bean.base.BaseQuery;
import lombok.Data;

import java.util.List;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2025/11/1 23:50
 */
@Data
public class FormFieldQuery extends BaseQuery {

    /**
     * 表单id
     */
    private Long formId;

    /**
     * 表单版本id
     */
    private Long revId;

    /**
     * 字段编码
     */
    private String code;

    /**
     * 字段编码
     */
    private List<String> codes;
}