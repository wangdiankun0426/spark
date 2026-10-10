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
 * @since 2026/10/10 10:00:00
 * 技能文件返回结果
 */
@Data
public class SkillFileResult extends BaseResult {

    /**
     * 技能id
     **/
    private Long skillId;

    /**
     * 文件虚拟路径
     **/
    private String filePath;

    /**
     * 文件用途描述
     **/
    private String description;

    /**
     * 文件内容
     **/
    private String content;

    /**
     * 排序
     **/
    private Integer sort;

}
