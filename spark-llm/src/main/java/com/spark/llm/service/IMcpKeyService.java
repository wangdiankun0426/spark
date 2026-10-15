package com.spark.llm.service;

import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.llm.query.McpKeyQuery;
import com.spark.common.bean.llm.result.McpKeyResult;
import com.spark.common.bean.llm.vo.McpKeyVO;

import java.util.List;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-10-15 14:10:00
 */
public interface IMcpKeyService {

    /**
     * 申请MCP密钥
     * @param mcpKeyVO MCP密钥数据
     * @return 创建结果，data为生成的完整密钥
     */
    ResultData<String> createMcpKey(McpKeyVO mcpKeyVO);

    /**
     * 删除MCP密钥
     * @param mcpKeyVO MCP密钥数据
     * @return 删除结果
     */
    ResultData<Void> deleteMcpKey(McpKeyVO mcpKeyVO);

    /**
     * 查询当前用户的MCP密钥列表
     * @return 密钥列表
     */
    ResultData<List<McpKeyResult>> queryMyKeyList();

    /**
     * 校验API密钥有效性
     * @param keyValue API密钥
     * @return 校验结果，data为密钥信息
     */
    ResultData<McpKeyResult> validateApiKey(String keyValue);

}
