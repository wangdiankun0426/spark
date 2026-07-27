package com.spark.bean.log.result;

import com.spark.bean.base.BaseResult;
import lombok.Data;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/4/3 19:28
 */
@Data
public class LogOperateResult extends BaseResult {
    /**
     * 操作类型
     */
    private Integer type;

    /**
     * 操作类型
     */
    private String typeName;

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
