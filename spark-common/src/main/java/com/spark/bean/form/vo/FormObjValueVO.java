package com.spark.bean.form.vo;

import com.spark.bean.base.BaseVO;
import com.spark.bean.form.entity.FormObjValue;
import lombok.Data;

import java.util.List;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/7/28 20:50
 */
@Data
public class FormObjValueVO extends BaseVO {
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
    private List<FormObjValue> values;
}
