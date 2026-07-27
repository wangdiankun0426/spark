package com.spark.bean.form.query;

import com.spark.bean.base.BaseQuery;
import lombok.Data;

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
}
