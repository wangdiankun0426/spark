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
 * @since 2026/09/02 09:00:00
 * 技能返回结果
 */
@Data
public class SkillResult extends BaseResult {

    /**
     * 名称（唯一）
     **/
    private String name;

    /**
     * 一句话描述（注入装配目录，供模型判断是否命中）
     **/
    private String description;

    /**
     * 指令正文（Markdown）
     **/
    private String content;

    /**
     * 状态：1:启用，-1:禁用
     **/
    private Integer status;

    /**
     * 状态名称
     **/
    private String statusName;

}
