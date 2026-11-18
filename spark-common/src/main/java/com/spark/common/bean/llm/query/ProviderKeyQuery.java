package com.spark.common.bean.llm.query;

import com.spark.common.bean.base.BaseQuery;
import lombok.Data;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-11-18 15:00:00
 * 模型厂商密钥查询参数
 */
@Data
public class ProviderKeyQuery extends BaseQuery {

    /**
     * 厂商id
     */
    private Long providerId;

    /**
     * 状态
     */
    private Integer status;

}
