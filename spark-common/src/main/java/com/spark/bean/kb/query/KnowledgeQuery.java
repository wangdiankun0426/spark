package com.spark.bean.kb.query;

import com.spark.bean.base.BaseQuery;
import lombok.Data;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-07-17 10:00:00
 * 知识库查询
 */
@Data
public class KnowledgeQuery extends BaseQuery {

    /**
     * 名称
     */
    private String name;

    /**
     * 状态
     */
    private Integer status;

}
