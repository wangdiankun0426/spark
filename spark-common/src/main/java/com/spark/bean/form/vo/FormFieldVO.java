package com.spark.bean.form.vo;

import com.spark.bean.base.BaseVO;
import lombok.Data;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/7/3 16:57
 */
@Data
public class FormFieldVO extends BaseVO {

    /**
     * 字段名
     */
    private String code;

    /**
     * 字段类型
     */
    private String type;

    /**
     * 字段标题
     */
    private String label;
}
