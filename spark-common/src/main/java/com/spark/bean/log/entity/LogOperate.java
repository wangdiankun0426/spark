package com.spark.bean.log.entity;

import com.spark.bean.base.BaseEntity;
import lombok.Data;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/4/3 16:41
 */
@Data
public class LogOperate extends BaseEntity {

    /**
     * 所属模块
     */
    private Integer module;

    /**
     * 操作类型
     */
    private Integer type;

    /**
     * 操作对象id
     */
    private Long objId;

    /**
     * 状态码
     */
    private Integer code;

    /**
     * 耗时
     */
    private Integer consume;

    /**
     * 备注
     */
    private String remark;
}
