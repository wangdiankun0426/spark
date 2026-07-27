package com.spark.bean.kg.query;

import com.spark.bean.base.BaseQuery;
import lombok.Data;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-07-22 11:00:00
 * 知识图谱查询条件
 */
@Data
public class KgGraphQuery extends BaseQuery {

    /**
     * 图谱名称
     */
    private String name;

    /**
     * 状态（0-禁用 1-启用）
     */
    private Integer status;

}
