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

    /**
     * 字段code
     */
    private List<String> codes;

}
