package com.spark.bean.log.query;

import com.spark.bean.base.BaseQuery;
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
public class LogOperateQuery extends BaseQuery {
    /**
     * 类型
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
}
