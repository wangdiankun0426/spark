package com.spark.bean.llm.vo;

import com.spark.bean.base.BaseVO;
import lombok.Data;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026/09/02 09:00:00
 * 技能入参
 */
@Data
public class SkillVO extends BaseVO {

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

}
