package com.spark.common.bean.form.vo;

import com.spark.common.bean.base.BaseVO;
import lombok.Data;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024-07-01 09:11:13
 */
@Data
public class FormVO extends BaseVO {
    /**
     * 类型 FormTypeEnum
     */
    private Integer type;

    /**
     * 名称
     **/
    private String name;

    /**
     * 表单json
     **/
    private String formJson;

    /**
     * 排序号
     **/
    private Integer orderNum;

}
