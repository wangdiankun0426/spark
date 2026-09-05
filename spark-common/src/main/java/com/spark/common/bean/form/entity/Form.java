package com.spark.common.bean.form.entity;

import com.spark.common.bean.base.BaseEntity;
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
public class Form extends BaseEntity {
    /**
     * 类型 FormTypeEnum
     */
    private Integer type;

    /**
     * 名称
     **/
    private String name;

    /**
     * 当前版本id
     */
    private Long revId;

    /**
     * 当前版本号
     */
    private String revNum;

    /**
     * 排序号
     **/
    private Integer orderNum;

}
