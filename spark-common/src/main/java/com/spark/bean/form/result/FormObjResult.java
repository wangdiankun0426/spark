package com.spark.bean.form.result;

import com.spark.bean.base.BaseResult;
import lombok.Data;

import java.util.List;

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
public class FormObjResult extends BaseResult {
    /**
     * 对象id
     */
    private Long objId;
    /**
     * 表单id
     */
    private Long formId;

    /**
     * 表单值
     */
    private List<FormObjValueResult> values;

    /**
     * 表单json
     */
    private String formJson;
}
