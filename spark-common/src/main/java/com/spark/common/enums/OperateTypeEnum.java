package com.spark.common.enums;

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
    USER_DETAIL(14, "查询用户详情"),
    // 部门相关的操作记录
    DEPT_INSERT(20, "新增部门"),
    DEPT_UPDATE(21, "修改部门"),
    DEPT_DELETE(22, "删除部门"),
    // 租户相关的操作记录
    TENANT_INSERT(23, "新增租户"),
    TENANT_UPDATE(24, "修改租户"),
    TENANT_DELETE(25, "删除租户"),
    TENANT_ADD_USER(26, "为租户添加用户"),
    TENANT_DEL_USER(27, "为租户移除用户"),
    TENANT_UPDATE_USER_ROLE(28, "修改租户用户角色"),
    // 角色相关的操作记录
    ROLE_INSERT(30, "新增角色"),
    ROLE_UPDATE(31, "修改角色"),
    ROLE_DELETE(32, "删除角色"),
    ROLE_ADD_USER(33, "为角色添加用户"),
    ROLE_DEL_USER(34, "为角色移除用户"),
    // 公告相关的操作记录
    NOTICE_INSERT(40, "新增公告"),
    NOTICE_UPDATE(41, "修改公告"),
    NOTICE_DELETE(42, "删除公告"),
    NOTICE_DETAIL(43, "查询公告详情"),
    NOTICE_DELIST(44, "下架公告"),
    NOTICE_SAVE_TEXT(45, "保存公告内容"),
    NOTICE_VIEW_TEXT(46, "查看公告内容"),
    // 消息相关的操作记录
    MESSAGE_INSERT(50, "发送消息"),
    MESSAGE_DELETE(51, "删除消息"),
    // 附件相关的操作记录
    ATTACHMENT_UPLOAD(60, "上传附件"),
    ATTACHMENT_DOWNLOAD(61, "下载附件"),
    ATTACHMENT_DETAIL(61, "查询附件详情"),
    // 文档相关的操作记录
    DOCUMENT_INSERT(70, "新增文档"),
    DOCUMENT_UPDATE(71, "修改文档"),
    DOCUMENT_DELETE(72, "删除文档"),
    DOCUMENT_DETAIL(73, "查询文档详情"),
    DOCUMENT_SEARCH(74, "文档检索"),
    DOCUMENT_EVENT_UPDATE(75, "修改文档事件"),
    DOCUMENT_EVENT_DETAIL(76, "查询文档事件详情"),
    DOCUMENT_CHUNK_EDIT(77, "编辑文档分块内容"),
    // 知识库相关的操作记录
    KNOWLEDGE_INSERT(80, "新增知识库"),
    KNOWLEDGE_UPDATE(81, "修改知识库"),
    KNOWLEDGE_DELETE(82, "删除知识库"),
    KNOWLEDGE_DETAIL(83, "查询知识库详情"),
    // MCP服务器相关的操作记录
    MCP_INSERT(90, "新增MCP服务器"),
    MCP_UPDATE(91, "修改MCP服务器"),
    MCP_DELETE(92, "删除MCP服务器"),
    MCP_DETAIL(93, "MCP服务器详情"),
    MCP_TEST(94, "MCP服务器测试"),
    // 知识图谱相关的操作记录
    KG_GRAPH_INSERT(100, "新增知识图谱"),
    KG_GRAPH_UPDATE(101, "修改知识图谱"),
    KG_GRAPH_DELETE(102, "删除知识图谱"),
    KG_GRAPH_DETAIL(103, "查询知识图谱详情"),
    // 图谱实体相关的操作记录
    KG_ENTITY_UPDATE(110, "修改图谱实体"),
    KG_ENTITY_INSERT(111, "新增图谱实体"),
    KG_ENTITY_DELETE(112, "删除图谱实体"),
    KG_ENTITY_DETAIL(113, "查询图谱实体详情"),
    KG_ENTITY_MERGE(114, "合并图谱实体"),
    // 图谱关系相关的操作记录
    KG_RELATION_INSERT(120, "新增图谱关系"),
    KG_RELATION_UPDATE(121, "修改图谱关系"),
    KG_RELATION_DELETE(122, "删除图谱关系"),
    KG_RELATION_DETAIL(123, "查询图谱关系详情"),
    // workFlow相关操作记录
    WORKFLOW_INSERT(130, "新增workFlow"),
    WORKFLOW_UPDATE(131, "修改workFlow"),
    WORKFLOW_DELETE(132, "删除workFlow"),
    WORKFLOW_DETAIL(133, "查询workFlow详情"),
    WORKFLOW_VERSION_SAVE(134, "保存workFlow版本"),
    // 表单相关操作记录
    FORM_INSERT(140, "新增表单"),
    FORM_UPDATE(141, "修改表单"),
    FORM_DELETE(142, "删除表单"),
    FORM_DETAIL(143, "查询表单详情"),
    FORM_SAVE(144, "保存表单"),
    // 模型厂商相关操作
    PROVIDER_INSERT(150, "新增模型厂商"),
    PROVIDER_UPDATE(151, "修改模型厂商"),
    PROVIDER_DELETE(152, "删除模型厂商"),
    PROVIDER_DETAIL(153, "查询模型厂商详情"),
    // 模型相关操作
    MODEL_INSERT(160, "新增模型"),
    MODEL_UPDATE(161, "修改模型"),
    MODEL_DELETE(162, "删除模型"),
    MODEL_DETAIL(163, "查询模型详情"),
    // agent相关操作
    AGENT_INSERT(170, "新增智能体"),
    AGENT_UPDATE(171, "修改智能体"),
    AGENT_DELETE(172, "删除智能体"),
    AGENT_DETAIL(173, "查询智能体详情"),
    // skill相关操作
    SKILL_INSERT(180, "新增技能"),
    SKILL_UPDATE(181, "修改技能"),
    SKILL_DELETE(182, "删除技能"),
    SKILL_DETAIL(183, "查询技能详情")

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
