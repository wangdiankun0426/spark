package com.spark.llm.service.impl;

import com.spark.common.bean.base.PageResult;
import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.base.SessionHolder;
import com.spark.common.bean.llm.entity.Mcp;
import com.spark.common.bean.llm.query.McpQuery;
import com.spark.common.bean.llm.result.McpResult;
import com.spark.common.bean.llm.result.McpToolResult;
import com.spark.common.bean.llm.vo.McpVO;
import com.spark.config.aspectj.annotation.DataScope;
import com.spark.config.aspectj.annotation.LogIgnore;
import com.spark.config.aspectj.annotation.LogPrint;
import com.spark.config.aspectj.annotation.LogOperate;
import com.spark.dao.llm.McpDao;
import com.spark.common.enums.ErrorCodeEnum;
import com.spark.common.enums.McpTransportTypeEnum;
import com.spark.common.enums.OperateTypeEnum;
import com.spark.common.enums.StatusEnum;
import com.spark.llm.agent.AgentFactory;
import com.spark.llm.mcp.McpClientManager;
import com.spark.llm.security.LlmApiKeyCipher;
import com.spark.llm.service.IMcpService;
import com.spark.common.utils.CollectionUtil;
import com.spark.common.utils.StringUtil;
import com.spark.manage.BaseService;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.mcp.client.McpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.spark.common.utils.BeanUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026/07/19 15:40:00
 */
@Service
@LogPrint
public class McpServiceImpl extends BaseService<McpQuery, McpResult> implements IMcpService {
    private final static Logger logger = LoggerFactory.getLogger(McpServiceImpl.class);
    @Autowired
    private McpDao mcpDao;
    @Autowired
    private McpClientManager mcpClientManager;
    @Autowired
    private AgentFactory agentFactory;
    @Autowired
    private LlmApiKeyCipher llmApiKeyCipher;

    /**
     * 创建MCP服务器
     * @param mcpVO MCP服务器数据
     * @return 创建结果
     */
    @Override
    @LogIgnore
    @LogOperate(operateType = OperateTypeEnum.MCP_INSERT)
    public ResultData<Void> createMcp(McpVO mcpVO) {
        ResultData<Void> result = new ResultData<>();
        if (mcpVO == null || StringUtil.isBlank(mcpVO.getName()) || mcpVO.getTransport() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        if (!this.validateTransport(mcpVO.getTransport(), mcpVO.getUrl())) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        String plainKey = mcpVO.getSecretKey();
        if (StringUtil.isBlank(plainKey)) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        if (plainKey.length() > 256) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        Mcp mcp = new Mcp();
        BeanUtil.copyProperties(mcpVO, mcp);
        String encryptedKey = llmApiKeyCipher.encrypt(plainKey);
        if (encryptedKey.length() > 512) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        mcp.setSecretKey(encryptedKey);
        // 非站点管理员强制归属当前租户
        if (!SessionHolder.isSysAdmin()) {
            mcp.setTenantId(SessionHolder.getCurrentTenantId());
        }
        if (mcp.getStatus() == null) {
            mcp.setStatus(StatusEnum.NORMAL.getValue());
        }
        int count = mcpDao.insertDB(mcp);
        if (count < 1) {
            result.setErrorCode(ErrorCodeEnum.INSERT_DATA_FAIL);
            return result;
        }
        result.setObjId(mcp.getId());
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 修改MCP服务器
     * @param mcpVO MCP服务器数据
     * @return 修改结果
     */
    @Override
    @LogIgnore
    @LogOperate(operateType = OperateTypeEnum.MCP_UPDATE)
    public ResultData<Void> updateMcp(McpVO mcpVO) {
        ResultData<Void> result = new ResultData<>();
        if (mcpVO == null || mcpVO.getId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        McpQuery query = new McpQuery();
        query.setId(mcpVO.getId());
        super.scopeTenant(query);
        Mcp existMcp = mcpDao.queryMcpConfig(query);
        if (existMcp == null) {
            result.setErrorCode(ErrorCodeEnum.MCP_NOT_EXIST);
            return result;
        }
        if (mcpVO.getTransport() != null && !this.validateTransport(mcpVO.getTransport(), mcpVO.getUrl())) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        String plainKey = mcpVO.getSecretKey();
        if (StringUtil.isBlank(existMcp.getSecretKey()) && StringUtil.isBlank(plainKey)) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        if (StringUtil.isNotBlank(plainKey) && plainKey.length() > 256) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        Mcp mcp = new Mcp();
        BeanUtil.copyProperties(mcpVO, mcp);
        if (StringUtil.isNotBlank(plainKey)) {
            String encryptedKey = llmApiKeyCipher.encrypt(plainKey);
            if (encryptedKey.length() > 512) {
                result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
                return result;
            }
            mcp.setSecretKey(encryptedKey);
        } else {
            mcp.setSecretKey(null);
        }
        int count = mcpDao.updateDBById(mcp);
        if (count < 1) {
            result.setErrorCode(ErrorCodeEnum.UPDATE_DATA_FAIL);
            return result;
        }
        // 配置变更后断开旧连接，下次使用时会按新配置重连
        mcpClientManager.disconnect(mcpVO.getId());
        agentFactory.clearAllAgentCache();
        result.setObjId(mcp.getId());
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 删除MCP服务器
     * @param mcpVO MCP服务器数据
     * @return 删除结果
     */
    @Override
    @LogOperate(operateType = OperateTypeEnum.MCP_DELETE)
    public ResultData<Void> deleteMcp(McpVO mcpVO) {
        ResultData<Void> result = new ResultData<>();
        if (mcpVO == null || mcpVO.getId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        McpQuery query = new McpQuery();
        query.setId(mcpVO.getId());
        super.scopeTenant(query);
        McpResult existResult = mcpDao.queryMcp(query);
        if (existResult == null) {
            result.setCode(ResultData.OK);
            return result;
        }
        Mcp Mcp = new Mcp();
        Mcp.setId(mcpVO.getId());
        int count = mcpDao.deleteDBById(Mcp);
        if (count < 1) {
            result.setErrorCode(ErrorCodeEnum.DELETE_DATA_FAIL);
            return result;
        }
        // 同步断开MCP客户端连接
        mcpClientManager.disconnect(mcpVO.getId());
        agentFactory.clearAllAgentCache();
        result.setObjId(mcpVO.getId());
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 分页查询MCP服务器
     * @param query 查询条件
     * @return 分页结果
     */
    @Override
    @DataScope
    public ResultData<PageResult<McpResult>> pageMcpList(McpQuery query) {
        ResultData<PageResult<McpResult>> result = new ResultData<>();
        if (query == null) {
            query = new McpQuery();
        }
        PageResult<McpResult> list = super.pageList(query);
        result.setData(list);
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 查询MCP服务器详情
     * @param query 查询条件
     * @return 详情
     */
    @Override
    @LogOperate(operateType = OperateTypeEnum.MCP_DETAIL)
    public ResultData<McpResult> queryMcpDetail(McpQuery query) {
        ResultData<McpResult> result = new ResultData<>();
        if (query == null || query.getId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        super.scopeTenant(query);
        McpResult mcpResult = mcpDao.queryMcp(query);
        if (mcpResult == null) {
            result.setErrorCode(ErrorCodeEnum.MCP_NOT_EXIST);
            return result;
        }
        supplySingle(mcpResult);
        result.setData(mcpResult);
        result.setObjId(mcpResult.getId());
        result.setCode(ResultData.OK);
        return result;
    }

    /**
     * 测试MCP服务器连通性
     * @param mcpVO MCP服务器数据
     * @return 测试结果
     */
    @Override
    @LogOperate(operateType = OperateTypeEnum.MCP_TEST)
    public ResultData<Void> test(McpVO mcpVO) {
        ResultData<Void> result = new ResultData<>();
        if (mcpVO == null || mcpVO.getId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        McpQuery mcpQuery = new McpQuery();
        mcpQuery.setId(mcpVO.getId());
        super.scopeTenant(mcpQuery);
        Mcp mcp = mcpDao.queryMcpConfig(mcpQuery);
        if (mcp == null) {
            result.setErrorCode(ErrorCodeEnum.MCP_NOT_EXIST);
            return result;
        }
        mcp.setStatus(StatusEnum.NORMAL.getValue());
        String mcpName = mcp.getName();
        try {
            McpClient client = mcpClientManager.connectToServer(mcp);
            if (client == null) {
                result.setErrorCode(ErrorCodeEnum.MCP_CONNECT_FAIL);
                return result;
            }
            // 触发listTools验证连通性
            int toolCount = client.listTools().size();
            logger.info("mcp connect success, name={}, tools={}", mcpName, toolCount);
            // 测试完毕断开（避免占用资源，正式使用时按需重连）
            mcpClientManager.disconnect(mcpVO.getId());
            result.setObjId(mcpVO.getId());
            result.setCode(ResultData.OK);
            return result;
        } catch (Exception e) {
            logger.error("mcp connect fail, name={}", mcpName, e);
            result.setErrorCode(ErrorCodeEnum.MCP_CONNECT_FAIL);
            return result;
        }
    }

    /**
     * 查询MCP服务器可用工具列表
     * @param query 查询条件
     * @return 工具列表
     */
    @Override
    public ResultData<List<McpToolResult>> queryMcpToolList(McpQuery query) {
        ResultData<List<McpToolResult>> result = new ResultData<>();
        if (query == null || query.getId() == null) {
            result.setErrorCode(ErrorCodeEnum.INVALID_PARAM);
            return result;
        }
        super.scopeTenant(query);
        Mcp mcp = mcpDao.queryMcpConfig(query);
        if (mcp == null) {
            result.setErrorCode(ErrorCodeEnum.MCP_NOT_EXIST);
            return result;
        }
        // 停用状态也允许查看工具，与测试连通逻辑一致
        mcp.setStatus(StatusEnum.NORMAL.getValue());
        String mcpName = mcp.getName();
        try {
            McpClient client = mcpClientManager.connectToServer(mcp);
            if (client == null) {
                result.setErrorCode(ErrorCodeEnum.MCP_CONNECT_FAIL);
                return result;
            }
            List<ToolSpecification> tools = client.listTools();
            List<McpToolResult> toolList = tools.stream().map(tool -> {
                McpToolResult toolResult = new McpToolResult();
                toolResult.setName(tool.name());
                toolResult.setDescription(tool.description());
                return toolResult;
            }).collect(Collectors.toList());
            logger.info("queryMcpToolList success, id={}, size={}", query.getId(), toolList.size());
            result.setData(toolList);
            result.setCode(ResultData.OK);
            return result;
        } catch (Exception e) {
            logger.error("queryMcpToolList fail, name={}", mcpName, e);
            result.setErrorCode(ErrorCodeEnum.MCP_CONNECT_FAIL);
            return result;
        }
    }

    /**
     * 校验传输类型与配套参数
     * @param transport 传输类型
     * @param url HTTP地址
     * @return 校验结果
     */
    private boolean validateTransport(Integer transport, String url) {
        if (McpTransportTypeEnum.HTTP.getValue().equals(transport)) {
            return StringUtil.isNotBlank(url);
        }
        return false;
    }

    /**
     * 补充单条数据
     * @param mcpResult 数据
     */
    private void supplySingle(McpResult mcpResult) {
        mcpResult.setTransportName(McpTransportTypeEnum.indexOf(mcpResult.getTransport()).getDesc());
        mcpResult.setStatusName(StatusEnum.indexOf(mcpResult.getStatus()).getDesc());
    }

    /**
     * 补充列表数据
     * @param list 列表
     */
    @Override
    protected void supplyList(List<McpResult> list) {
        if (CollectionUtil.isEmpty(list)) {
            return;
        }
        super.supplyCreatedByName(list);
        super.supplyUpdatedByName(list);
        for (McpResult mcpResult : list) {
            mcpResult.setTransportName(McpTransportTypeEnum.indexOf(mcpResult.getTransport()).getDesc());
            mcpResult.setStatusName(StatusEnum.indexOf(mcpResult.getStatus()).getDesc());
        }
    }

    /**
     * 查询数量
     * @param query 查询参数
     * @return 数量
     */
    @Override
    protected int queryCount(McpQuery query) {
        return mcpDao.queryMcpCount(query);
    }

    /**
     * 查询列表
     * @param query 查询参数
     * @return 列表
     */
    @Override
    protected List<McpResult> queryList(McpQuery query) {
        return mcpDao.queryMcpList(query);
    }
}
