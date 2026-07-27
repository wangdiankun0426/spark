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
 * @since 2026/7/15 20:51
 */
@Data
public class FormObjQuery extends BaseQuery {
    /**
     * 对象id
     */
    private Long objId;
    /**
     * 表单id
     */
    private Long formId;
}
