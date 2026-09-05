package com.spark.common.bean.form.query;

import com.spark.common.bean.base.BaseQuery;
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
public class FormQuery extends BaseQuery {
    /**
     * 类型 FormTypeEnum
     */
    private Integer type;

    /**
     * 名称
     */
    private String name;

    /**
     * 版本id
     */
    private Long revId;

    /**
     * 排序号
     */
    private Integer orderNum;

}
