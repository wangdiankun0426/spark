package com.spark.bean.llm.entity;

import com.spark.bean.base.BaseEntity;
import lombok.Data;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-05-13 20:13:03
 */
@Data
public class Provider extends BaseEntity {

    /**
     * 厂商名称
     **/
    private String name;

    /**
     * 厂商图标
     **/
    private String icon;

    /**
     * API地址
     **/
    private String apiUrl;

    /**
     * 厂商密钥
     **/
    private String secretKey;

    /**
     * 厂商描述
     **/
    private String description;

    /**
     * 备注
     **/
    private String remark;

    /**
     * 排序
     **/
    private Integer orderNum;

}
