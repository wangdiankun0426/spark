package com.spark.common.bean.llm.vo;

import com.spark.common.bean.base.BaseVO;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.ToString;

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
public class ProviderVO extends BaseVO {

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
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @ToString.Exclude
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
