package com.spark.common.bean.llm.result;

import com.spark.common.bean.base.BaseResult;
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
public class ProviderResult extends BaseResult {

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

    /**
     * 熔断状态值
     **/
    private Integer circuitState;

    /**
     * 熔断状态名称
     **/
    private String circuitStateName;

}
