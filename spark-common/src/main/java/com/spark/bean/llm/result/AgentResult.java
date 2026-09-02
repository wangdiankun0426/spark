package com.spark.bean.llm.result;

import com.spark.bean.base.BaseResult;
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
public class AgentResult extends BaseResult {

    /**
     * 名称
     **/
    private String name;

    /**
     * 语言模型ID
     **/
    private Long chatModelId;

    /**
     * 语言模型名称
     */
    private String chatModelName;

    /**
     * 系统提示词
     **/
    private String systemPrompt;

    /**
     * 对话记忆大小
     **/
    private Integer maxMessages;

    /**
     * 工具
     **/
    private String tools;

    /**
     * 知识库ID列表
     **/
    private String kbIds;

    /**
     * 知识图谱ID列表
     **/
    private String graphIds;

    /**
     * MCP ID列表
     **/
    private String mcpIds;

    /**
     * 技能ID列表
     **/
    private String skills;

    /**
     * 描述
     **/
    private String description;

    /**
     * 状态
     **/
    private Integer status;

    /**
     * 状态名称
     **/
    private String statusName;

    /**
     * 工具名称
     **/
    private String toolNames = "无";

    /**
     * 知识库名称
     **/
    private String kbNames = "无";

    /**
     * 知识图谱名称
     **/
    private String graphNames = "无";

    /**
     * MCP名称
     **/
    private String mcpNames = "无";

    /**
     * 技能名称
     **/
    private String skillNames = "无";

}
