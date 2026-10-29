package com.spark.llm.mcp;

import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.base.SessionHolder;
import com.spark.common.bean.dms.query.DocumentQuery;
import com.spark.common.bean.dms.result.DocumentResult;
import com.spark.common.bean.kb.query.KnowledgeQuery;
import com.spark.common.bean.kb.result.KnowledgeResult;
import com.spark.common.bean.kg.query.KgGraphQuery;
import com.spark.common.bean.kg.result.KgGraphResult;
import com.spark.common.bean.llm.result.McpKeyResult;
import com.spark.common.bean.llm.result.RetrieveResult;
import com.spark.common.bean.sys.query.DepartmentQuery;
import com.spark.common.bean.sys.query.TenantUserQuery;
import com.spark.common.bean.sys.result.DepartmentResult;
import com.spark.common.bean.sys.result.TenantUserResult;
import com.spark.common.utils.CollectionUtil;
import com.spark.common.utils.MapUtil;
import com.spark.common.utils.StringUtil;
import com.spark.config.aspectj.annotation.DataScope;
import com.spark.dao.dms.DocumentDao;
import com.spark.dao.kb.KnowledgeDao;
import com.spark.dao.kg.KgGraphDao;
import com.spark.dao.llm.McpKeyDao;
import com.spark.dao.sys.DepartmentDao;
import com.spark.dao.sys.RoleDao;
import com.spark.dao.sys.TenantUserDao;
import com.spark.common.kb.IKbRetriever;
import com.spark.common.kg.IKgRetriever;
import com.spark.llm.service.IMcpKeyService;
import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-10-15 14:20:00
 * MCP服务端工具执行器
 * 按密钥绑定用户的数据权限对外提供检索工具
 */
@Component
public class McpServerExecutor {
    private final static Logger logger = LoggerFactory.getLogger(McpServerExecutor.class);
    /**
     * 传输上下文中Authorization头的key
     */
    public final static String CONTEXT_AUTHORIZATION = "authorization";
    /**
     * Authorization头前缀
     */
    private final static String BEARER_PREFIX = "Bearer ";
    @Autowired
    private IMcpKeyService mcpKeyService;
    @Autowired
    private IKbRetriever kbRetrieve;
    @Autowired
    private IKgRetriever kgRetrieve;
    @Autowired
    private KnowledgeDao knowledgeDao;
    @Autowired
    private KgGraphDao kgGraphDao;
    @Autowired
    private DocumentDao documentDao;
    @Autowired
    private McpKeyDao mcpKeyDao;
    @Autowired
    private TenantUserDao tenantUserDao;
    @Autowired
    private RoleDao roleDao;
    @Autowired
    private DepartmentDao departmentDao;
    /**
     * 自注入走代理调用，同类内直接调用会使@DataScope切面失效
     */
    @Autowired
    @Lazy
    private McpServerExecutor selfProxy;

    /**
     * 构建对外暴露的工具列表
     * @return 工具规格列表
     */
    public List<McpServerFeatures.SyncToolSpecification> toolSpecifications() {
        List<McpServerFeatures.SyncToolSpecification> list = new ArrayList<>();
        list.add(this.searchKnowledgeSpec());
        list.add(this.searchKgSpec());
        return list;
    }

    /**
     * 查询数据权限范围内的知识库列表
     * @param query 查询条件
     * @return 知识库列表
     */
    @DataScope
    public ResultData<List<KnowledgeResult>> queryScopeKnowledgeList(KnowledgeQuery query) {
        ResultData<List<KnowledgeResult>> result = new ResultData<>();
        if (query == null) {
            query = new KnowledgeQuery();
        }
        query.setTenantId(SessionHolder.getCurrentTenantId());
        query.setPage(false);
        List<KnowledgeResult> list = knowledgeDao.queryKnowledgeList(query);
        result.setData(list);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 查询数据权限范围内的知识图谱列表
     * @param query 查询条件
     * @return 知识图谱列表
     */
    @DataScope
    public ResultData<List<KgGraphResult>> queryScopeKgGraphList(KgGraphQuery query) {
        ResultData<List<KgGraphResult>> result = new ResultData<>();
        if (query == null) {
            query = new KgGraphQuery();
        }
        query.setTenantId(SessionHolder.getCurrentTenantId());
        query.setPage(false);
        List<KgGraphResult> list = kgGraphDao.queryKgGraphList(query);
        result.setData(list);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 知识库检索工具
     * @return 工具规格
     */
    private McpServerFeatures.SyncToolSpecification searchKnowledgeSpec() {
        String schema = "{\"type\":\"object\",\"properties\":{\"query\":{\"type\":\"string\",\"description\":\"检索问题或关键词\"}},\"required\":[\"query\"]}";
        McpSchema.Tool tool = McpSchema.Tool.builder()
                .name("search_knowledge")
                .description("知识库文档检索工具。当问题需要从文档资料中查找具体内容、条款、说明时使用，返回相关文本片段。每个问题最多调用一次本工具，可与search_kg工具配合使用。")
                .inputSchema(McpJsonMapper.getDefault(), schema)
                .build();
        return McpServerFeatures.SyncToolSpecification.builder()
                .tool(tool)
                .callHandler(this::handleSearchKnowledge)
                .build();
    }

    /**
     * 知识图谱检索工具
     * @return 工具规格
     */
    private McpServerFeatures.SyncToolSpecification searchKgSpec() {
        String schema = "{\"type\":\"object\",\"properties\":{\"question\":{\"type\":\"string\",\"description\":\"用户问题\"}},\"required\":[\"question\"]}";
        McpSchema.Tool tool = McpSchema.Tool.builder()
                .name("search_kg")
                .description("知识图谱检索工具。当问题涉及人物、组织、概念等实体之间的关系时使用，返回实体关系三元组。每个问题最多调用一次本工具，可与search_knowledge工具配合使用。")
                .inputSchema(McpJsonMapper.getDefault(), schema)
                .build();
        return McpServerFeatures.SyncToolSpecification.builder()
                .tool(tool)
                .callHandler(this::handleSearchKg)
                .build();
    }

    /**
     * 执行知识库检索
     * @param exchange 服务端交换对象
     * @param request 调用请求
     * @return 调用结果
     */
    private McpSchema.CallToolResult handleSearchKnowledge(McpSyncServerExchange exchange, McpSchema.CallToolRequest request) {
        McpKeyResult mcpKey = this.authorize(exchange);
        if (mcpKey == null) {
            return new McpSchema.CallToolResult("鉴权失败：API密钥无效或已过期", true);
        }
        Map<String, Object> arguments = request.arguments();
        String query = MapUtil.getStringVal(arguments, "query");
        if (StringUtil.isBlank(query)) {
            return new McpSchema.CallToolResult("检索关键词不能为空", true);
        }
        try {
            this.initSessionContext(mcpKey);
            KnowledgeQuery knowledgeQuery = new KnowledgeQuery();
            ResultData<List<KnowledgeResult>> kbListResult = selfProxy.queryScopeKnowledgeList(knowledgeQuery);
            List<KnowledgeResult> kbList = kbListResult.getData();
            if (CollectionUtil.isEmpty(kbList)) {
                return new McpSchema.CallToolResult("当前用户没有可检索的知识库", true);
            }
            List<Long> kbIds = kbList.stream().map(KnowledgeResult::getId).collect(Collectors.toList());
            KnowledgeResult knowledge = kbList.get(0);
            RetrieveResult retrieval = kbRetrieve.retrieve(query, kbIds, knowledge);
            List<String> relevantDocs = retrieval.getContents();
            if (CollectionUtil.isEmpty(relevantDocs)) {
                return new McpSchema.CallToolResult("根据当前知识库，无法找到与您的问题相关的内容。", false);
            }
            String docNames = this.queryDocNames(retrieval.getReferences());
            String content = "【检索完成，共 " + relevantDocs.size() + " 条相关内容】\n\n"
                    + String.join("\n\n", relevantDocs)
                    + "\n\n【检索结束】"
                    + (StringUtil.isNotBlank(docNames) ? "\n\n【参考文档】" + docNames : "");
            logger.info("mcp searchKnowledge success, mcpKeyId={}, size={}", mcpKey.getId(), relevantDocs.size());
            return new McpSchema.CallToolResult(content, false);
        } catch (Exception e) {
            logger.error("mcp searchKnowledge error, mcpKeyId={}", mcpKey.getId(), e);
            return new McpSchema.CallToolResult("知识库检索过程中出现异常", true);
        } finally {
            SessionHolder.clearLocalSession();
        }
    }

    /**
     * 执行知识图谱检索
     * @param exchange 服务端交换对象
     * @param request 调用请求
     * @return 调用结果
     */
    private McpSchema.CallToolResult handleSearchKg(McpSyncServerExchange exchange, McpSchema.CallToolRequest request) {
        McpKeyResult mcpKey = this.authorize(exchange);
        if (mcpKey == null) {
            return new McpSchema.CallToolResult("鉴权失败：API密钥无效或已过期", true);
        }
        Map<String, Object> arguments = request.arguments();
        String question = MapUtil.getStringVal(arguments, "question");
        if (StringUtil.isBlank(question)) {
            return new McpSchema.CallToolResult("问题不能为空", true);
        }
        try {
            this.initSessionContext(mcpKey);
            KgGraphQuery graphQuery = new KgGraphQuery();
            ResultData<List<KgGraphResult>> graphListResult = selfProxy.queryScopeKgGraphList(graphQuery);
            List<KgGraphResult> graphList = graphListResult.getData();
            if (CollectionUtil.isEmpty(graphList)) {
                return new McpSchema.CallToolResult("当前用户没有可检索的知识图谱", true);
            }
            List<Long> graphIds = graphList.stream().map(KgGraphResult::getId).collect(Collectors.toList());
            ResultData<List<String>> retrieveResult = kgRetrieve.retrieve(question, graphIds);
            if (retrieveResult == null || retrieveResult.getCode() != ResultData.OK
                    || CollectionUtil.isEmpty(retrieveResult.getData())) {
                return new McpSchema.CallToolResult("根据当前知识图谱，无法找到与您的问题相关的内容。", false);
            }
            List<String> triples = retrieveResult.getData();
            logger.info("mcp searchKg success, mcpKeyId={}, size={}", mcpKey.getId(), triples.size());
            return new McpSchema.CallToolResult(String.join("\n", triples), false);
        } catch (Exception e) {
            logger.error("mcp searchKg error, mcpKeyId={}", mcpKey.getId(), e);
            return new McpSchema.CallToolResult("知识图谱检索过程中出现异常", true);
        } finally {
            SessionHolder.clearLocalSession();
        }
    }

    /**
     * 从传输上下文解析并校验API密钥
     * @param exchange 服务端交换对象
     * @return 密钥信息，无效返回null
     */
    private McpKeyResult authorize(McpSyncServerExchange exchange) {
        if (exchange == null || exchange.transportContext() == null) {
            return null;
        }
        Object authorization = exchange.transportContext().get(CONTEXT_AUTHORIZATION);
        if (authorization == null) {
            return null;
        }
        String keyValue = authorization.toString();
        if (keyValue.startsWith(BEARER_PREFIX)) {
            keyValue = keyValue.substring(BEARER_PREFIX.length());
        }
        if (StringUtil.isBlank(keyValue)) {
            return null;
        }
        ResultData<McpKeyResult> result = mcpKeyService.validateApiKey(keyValue);
        if (result == null || result.getCode() != ResultData.OK || result.getData() == null) {
            return null;
        }
        // 鉴权通过视为一次有效调用，累加使用次数
        mcpKeyDao.incrUseCount(result.getData().getId());
        return result.getData();
    }

    /**
     * 按密钥绑定用户初始化线程数据权限上下文
     * @param mcpKey 密钥信息
     */
    private void initSessionContext(McpKeyResult mcpKey) {
        SessionHolder.setCurrentTenantId(mcpKey.getTenantId());
        SessionHolder.setCurrentUserId(mcpKey.getUserId());
        TenantUserQuery tenantUserQuery = new TenantUserQuery();
        tenantUserQuery.setTenantId(mcpKey.getTenantId());
        tenantUserQuery.setUserId(mcpKey.getUserId());
        tenantUserQuery.setPage(false);
        List<TenantUserResult> tenantUserList = tenantUserDao.queryTenantUserList(tenantUserQuery);
        if (CollectionUtil.isNotEmpty(tenantUserList)) {
            TenantUserResult tenantUser = tenantUserList.get(0);
            SessionHolder.setCurrentDeptId(tenantUser.getDeptId());
            SessionHolder.setCurrentRoleType(tenantUser.getRoleType());
        }
        int dataScope = roleDao.queryUserMaxDataScope(mcpKey.getTenantId(), mcpKey.getUserId());
        SessionHolder.setCurrentDataScop(dataScope);
        Long deptId = SessionHolder.getCurrentDeptId();
        if (deptId == null) {
            return;
        }
        DepartmentQuery departmentQuery = new DepartmentQuery();
        departmentQuery.setTenantId(mcpKey.getTenantId());
        departmentQuery.setId(deptId);
        DepartmentResult department = departmentDao.queryDepartment(departmentQuery);
        if (department == null) {
            return;
        }
        // 按部门编码前缀取其下级部门
        departmentQuery.setId(null);
        departmentQuery.setCode(department.getCode());
        List<DepartmentResult> departmentList = departmentDao.queryDepartmentList(departmentQuery);
        String deptIds = StringUtil.join(departmentList.stream().map(DepartmentResult::getId).toList(), ",");
        SessionHolder.setCurrentDeptIds(deptIds);
    }

    /**
     * 查询参考文档名称
     * @param docIds 文档id列表
     * @return 逗号分隔的文档名
     */
    private String queryDocNames(List<Long> docIds) {
        if (CollectionUtil.isEmpty(docIds)) {
            return "";
        }
        List<Long> distinctIds = docIds.stream().distinct().collect(Collectors.toList());
        DocumentQuery query = new DocumentQuery();
        query.setIds(distinctIds);
        List<DocumentResult> docList = documentDao.queryDocumentList(query);
        if (CollectionUtil.isEmpty(docList)) {
            return "";
        }
        return docList.stream().map(DocumentResult::getName).collect(Collectors.joining("、"));
    }
}
