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
 * @since 2026-05-17 10:12:34
 */
@Data
public class AgentQuery extends BaseQuery {

    /**
     * 名称
     */
    private String name;

    /**
     * 语言模型ID
     */
    private Long chatModelId;

    /**
     * 系统提示词
     */
    private String sysPrompt;

    /**
     * 对话记忆大小
     */
    private Integer maxMessages;

    /**
     * 工具
     */
    private String tools;

    /**
     * 知识库ID列表
     */
    private String kbIds;

    /**
     * 知识图谱ID列表
     */
    private String graphIds;

    /**
     * MCP ID列表
     */
    private String mcpIds;

    /**
     * 描述
     */
    private String description;

    /**
     * 状态
     */
    private Integer status;

}
