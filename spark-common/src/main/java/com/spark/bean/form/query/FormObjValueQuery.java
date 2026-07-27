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
 * @since 2024/7/28 21:53
 */
@Data
public class FormObjValueQuery extends BaseQuery {
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
}
