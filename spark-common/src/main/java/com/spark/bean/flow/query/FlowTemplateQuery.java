package com.spark.bean.flow.query;

import com.spark.bean.base.BaseQuery;
import lombok.Data;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2025-10-29 20:57:44
 */
@Data
public class FlowTemplateQuery extends BaseQuery {

    /**
     * 名称
     */
    private String name;

    /**
     * 模板id
     */
    private String processId;

    /**
     * 版本id
     **/
    private Long revId;

    /**
     * 版本
     **/
    private String revNum;

    /**
     * 状态
     */
    private Integer status;

}
