package com.spark.enums;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/4/3 17:07
 */
public enum OperateTypeEnum {
    // 其他操作记录
    UNKNOWN(1, "未知操作"),
    // 用户相关的操作记录
    USER_INSERT(10, "新增用户"),
    USER_UPDATE(11, "修改用户"),
    USER_DELETE(12, "删除用户"),
    USER_UPDATE_PASSWORD(13, "修改密码"),
    // 部门相关的操作记录
    DEPT_INSERT(20, "新增部门"),
    DEPT_UPDATE(21, "修改部门"),
    DEPT_DELETE(22, "删除部门"),
    // 角色相关的操作记录
    ROLE_INSERT(30, "新增角色"),
    ROLE_UPDATE(31, "修改角色"),
    ROLE_DELETE(32, "删除角色"),
    ROLE_ADD_USER(33, "为角色添加用户"),
    ROLE_DEL_USER(34, "为角色移除用户"),
    // 知识库相关的操作记录
    KNOWLEDGE_INSERT(50, "新增知识库"),
    KNOWLEDGE_UPDATE(51, "修改知识库"),
    KNOWLEDGE_DELETE(52, "删除知识库"),
    KNOWLEDGE_DOCUMENT_INSERT(53, "新增知识库文档"),
    KNOWLEDGE_DOCUMENT_UPDATE(54, "修改知识库文档"),
    KNOWLEDGE_DOCUMENT_DELETE(55, "删除知识库文档"),
    DOCUMENT_EVENT_UPDATE(56, "修改文档事件"),
    // MCP服务器相关的操作记录
    MCP_INSERT(60, "新增MCP服务器"),
    MCP_UPDATE(61, "修改MCP服务器"),
    MCP_DELETE(62, "删除MCP服务器"),
    // 知识图谱相关的操作记录
    KG_GRAPH_INSERT(70, "新增知识图谱"),
    KG_GRAPH_UPDATE(71, "修改知识图谱"),
    KG_GRAPH_DELETE(72, "删除知识图谱"),
    KG_ENTITY_UPDATE(73, "修改图谱实体"),
    KG_ENTITY_MERGE(74, "合并图谱实体"),
    KG_RELATION_UPDATE(75, "修改图谱关系"),
    KG_RELATION_DELETE(76, "删除图谱关系"),
    KG_ENTITY_INSERT(77, "新增图谱实体"),
    KG_ENTITY_DELETE(78, "删除图谱实体"),
    KG_RELATION_INSERT(79, "新增图谱关系"),
    // AI工作流相关操作记录
    WORKFLOW_INSERT(80, "新增AI工作流"),
    WORKFLOW_UPDATE(81, "修改AI工作流"),
    WORKFLOW_DELETE(82, "删除AI工作流"),
    WORKFLOW_PUBLISH(83, "发布AI工作流"),
    WORKFLOW_ENDPOINT_UPDATE(84, "修改端点配置"),
    ;
    private Integer value;

    private String desc;

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    OperateTypeEnum(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public static OperateTypeEnum indexOf(Integer value){
        for(OperateTypeEnum item:values()){
            if(item.getValue().equals(value)){
                return item;
            }
        }
        return UNKNOWN;
    }
}
