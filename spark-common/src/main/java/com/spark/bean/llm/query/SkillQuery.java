package com.spark.bean.llm.query;

import com.spark.bean.base.BaseQuery;
import lombok.Data;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026/09/02 09:00:00
 * 技能查询条件
 */
@Data
public class SkillQuery extends BaseQuery {

    /**
     * 名称
     */
    private String name;

    /**
     * 状态
     */
    private Integer status;

}
