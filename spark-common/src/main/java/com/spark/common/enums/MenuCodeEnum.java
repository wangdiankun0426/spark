package com.spark.common.enums;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026/9/10 16:30
 * 菜单标识码
 */
public enum MenuCodeEnum {


    /**
     * AI应用模块
     */
    MODULE_LLM(MenuTypeEnum.MODULE, "module.llm", "AI应用模块"),

    /**
     * 业务管理模块
     */
    MODULE_KB(MenuTypeEnum.MODULE, "module.bus", "业务管理模块"),


    // ============================== AI应用菜单 ==============================

    /**
     * Agent菜单
     */
    MENU_LLM_AGENT(MenuTypeEnum.MENU, "menu.llm.agent", "Agent菜单"),

    /**
     * WorkFlow菜单
     */
    MENU_LLM_WORKFLOW(MenuTypeEnum.MENU, "menu.llm.workflow", "WorkFlow菜单"),

    /**
     * 模型市场菜单
     */
    MENU_LLM_MODEL_MARKET(MenuTypeEnum.MENU, "menu.llm.modelMarket", "模型市场菜单"),

    /**
     * 技能库菜单
     */
    MENU_LLM_SKILL(MenuTypeEnum.MENU, "menu.llm.skill", "技能库菜单"),

    /**
     * MCP服务菜单
     */
    MENU_LLM_MCP(MenuTypeEnum.MENU, "menu.llm.mcp", "MCP服务菜单"),

    /**
     * 知识库菜单
     */
    MENU_KB_KNOWLEDGE(MenuTypeEnum.MENU, "menu.kb.knowledge", "知识库菜单"),

    /**
     * 知识图谱菜单
     */
    MENU_KG_GRAPH(MenuTypeEnum.MENU, "menu.kg.graph", "知识图谱菜单"),


    // ============================== 业务管理菜单 ==============================

    /**
     * 流程管理菜单
     */
    MENU_BUS_FLOW(MenuTypeEnum.MENU, "menu.bus.flow", "流程管理菜单"),

    ;

    private MenuTypeEnum type;

    private String code;

    private String desc;

    public MenuTypeEnum getType() {
        return type;
    }

    public void setType(MenuTypeEnum type) {
        this.type = type;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    MenuCodeEnum(MenuTypeEnum type, String code, String desc) {
        this.type = type;
        this.code = code;
        this.desc = desc;
    }

    public static MenuCodeEnum indexOf(String code) {
        for (MenuCodeEnum item : values()) {
            if (item.getCode().equals(code)) {
                return item;
            }
        }
        return null;
    }
}
