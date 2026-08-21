package com.spark.enums;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/1/25 17:58
 * .............................................
 * .............................................
 * 佛祖保佑             永无BUG
 */
public enum ErrorCodeEnum {
    UNKNOWN(0, "未知异常"),
    // 系统相关状态码
    INVALID_PARAM(400,"无效参数"),
    NOT_FOUND(404,"接口不存在"),
    SYSTEM_ERROR(500, "系统异常,请联系管理员"),
    MESSAGE_VALIDATE_SEND_ERROR(501, "短信验证码发送失败"),
    FILE_EXIST(502, "文件已存在"),
    FILE_NOT_EXIST(503, "文件不存在"),
    FILE_CREATE_FAIL(504, "文件创建失败"),
    NO_PERMISSION(506, "无操作权限"),
    // 登录相关状态码
    NOT_LOGIN(601, "用户未登录"),
    LONG_PASSWORD_ERROR(602, "密码错误"),
    LONG_NAME_NOT_EXIST(603, "登录名不存在"),
    VALIDATE_INVALID(604,"验证码已失效"),
    VALIDATE_CHECK_ERROR(605,"验证码错误"),
    PHONE_NOT_EXIST(606,"手机号不存在"),
    EMAIL_NOT_EXIST(607,"邮箱不存在"),
    LOGIN_TYPE_UNKNOWN(607,"未知登录类型"),
    // 用户相关状态码
    USER_SAME_LOGIN_NAME_EXIST(701,"相同登录名的用户已存在"),
    USER_NOT_EXIST(702, "用户不存在"),
    // 部门相关状态码
    DEPT_NOT_EXIST(801,"部门不存在"),
    DEPT_SAME_NAME_ALREADY_EXIST(802,"相同名称的部门已存在"),
    // 角色相关状态码
    ROLE_SAME_NAME_EXIST(901, "相同名称的角色已存在"),
    // 菜单相关状态码
    MENU_NOT_EXIST(1001, "菜单不存在"),
    SECOND_MENU_NOT_CREATE_SUB_MENU(1002, "二级菜单禁止创建子菜单"),
    // 公告相关状态码
    NOTICE_NOT_EXIST(1101, "公告不存在"),
    NOTICE_NOT_ALLOW(1102, "当前公告无操作权限"),
    NOTICE_NOT_DELIST_ALLOW(1103, "当前公告无下架权限"),
    // 聊天相关状态码
    CHAT_MSG_CREATE_ERROR(1201, "聊天消息创建失败"),
    CHAT_SPACE_NOT_EXIST(1202, "聊天空间不存在"),
    // 消息相关状态码
    MESSAGE_NOT_EXIST(1301, "消息不存在"),
    // 表单相关状态码
    FORM_NOT_EXIST(1401, "表单不存在"),
    FORM_VERSION_NOT_EXIST(1402, "表单版本不存在"),
    FORM_OBJ_EXIST(1403, "表单关联对象不存在"),
    // 附件相关状态码
    ATTACHMENT_NOT_EXIST(1501, "附件数据不存在"),
    // 文档相关状态码
    DOCUMENT_NOT_EXIST(1601, "文件数据不存在"),
    UPLOAD_FILE_NOT_EXIST(1602, "上传文件不能为空"),
    FILE_ES_INDEX_NOT_EXIST(1603, "文件索引模版不存在"),
    DOCUMENT_EVENT_NOT_EXIST(1606, "文件事件不存在"),
    DOCUMENT_METADATA_NOT_EXIST(1608, "文档元数据不存在"),
    DOCUMENT_METADATA_EXIST(1609, "文档元数据已存在"),
    // 流程相关状态码
    FLOW_TEMPLATE_NOT_EXIST(1701, "流程模板不存在"),
    FLOW_TEMPLATE_VERSION_NOT_EXIST(1702, "流程模板版本不存在"),
    FLOW_NODE_TYPE_UNKNOWN(1703, "未知流程节点类型"),
    FLOW_TEMPLATE_NOT_DEPLOYED(1704, "当前流程模板没有已部署的流程"),
    FLOW_INSTANCE_NOT_EXIST(1705, "流程实例不存在"),
    FLOW_INSTANCE_NOT_ALLOW(1706, "当前流程实例无操作权限"),
    FLOW_TASK_IS_EMPTY(1707, "当前流程实例任务不存在"),
    // 任务相关状态码
    TASK_RESTART(1801, "重启任务"),
    TASK_TEMPLATE_NOT_EXIST(1802, "任务模板不存在"),
    TASK_INSTANCE_NOT_EXIST(1803, "任务实例不存在"),
    // llm相关状态码
    PROVIDER_NOT_EXIST(2001, "模型厂商不存在"),
    MODEL_NOT_EXIST(2002, "模型不存在"),
    AGENT_NOT_EXIST(2003, "智能体不存在"),
    // 知识库相关状态码
    KNOWLEDGE_NOT_EXIST(2101, "知识库不存在"),
    KNOWLEDGE_DOCUMENT_NOT_EXIST(2102, "知识库文档不存在"),
    // MCP相关状态码
    MCP_NOT_EXIST(2201, "MCP服务不存在"),
    MCP_CONNECT_FAIL(2202, "MCP服务连接失败"),
    // 知识图谱相关状态码
    KG_GRAPH_NOT_EXIST(2301, "知识图谱不存在"),
    KG_ENTITY_NOT_EXIST(2302, "图谱实体不存在"),
    KG_RELATION_NOT_EXIST(2303, "图谱关系不存在"),
    KG_EXTRACT_FAIL(2304, "图谱抽取失败"),
    KG_GRAPH_STORE_FAIL(2305, "图存储操作失败"),
    // workflow相关状态码
    WORKFLOW_NOT_FOUND(2401, "WorkFlow不存在"),
    WORKFLOW_VERSION_NOT_FOUND(2402, "WorkFlow版本不存在"),
    WORKFLOW_NO_PUBLISHED_VERSION(2403, "WorkFlow无已发布版本"),
    WORKFLOW_DAG_INVALID(2406, "DAG结构非法"),
    WORKFLOW_RUN_NOT_FOUND(2407, "运行实例不存在"),
    WORKFLOW_NODE_EXEC_FAILED(2408, "节点执行失败"),
    WORKFLOW_NODE_TIMEOUT(2409, "节点执行超时"),
    WORKFLOW_ALREADY_PUBLISHED(2410, "WorkFlow已发布"),
    WORKFLOW_HAS_RUN_HISTORY(2411, "存在运行记录不允许删除"),
    WORKFLOW_CATEGORY_INVALID(2412, "分类标签无效"),

    ;

    private Integer value;
    private String desc;

    ErrorCodeEnum(Integer value, String desc){
        this.value = value;
        this.desc = desc;
    }

    public Integer getValue() {
        return value;
    }

    public String getDesc() {
        return desc;
    }

    public static ErrorCodeEnum indexOf(Integer value){
        for(ErrorCodeEnum item:values()){
            if(item.getValue().equals(value)){
                return item;
            }
        }
        return UNKNOWN;
    }
}
