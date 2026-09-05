package com.spark.llm.service;

import com.spark.common.bean.base.PageResult;
import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.llm.query.McpQuery;
import com.spark.common.bean.llm.result.McpResult;
import com.spark.common.bean.llm.vo.McpVO;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026/07/19 15:40:00
 */
public interface IMcpService {

    /**
     * 创建MCP服务器
     * @param mcpVO MCP服务器数据
     * @return 创建结果
     */
    ResultData<Void> createMcp(McpVO mcpVO);

    /**
     * 修改MCP服务器
     * @param mcpVO MCP服务器数据
     * @return 修改结果
     */
    ResultData<Void> updateMcp(McpVO mcpVO);

    /**
     * 删除MCP服务器
     * @param mcpVO MCP服务器数据
     * @return 删除结果
     */
    ResultData<Void> deleteMcp(McpVO mcpVO);

    /**
     * 分页查询MCP服务器
     * @param query 查询条件
     * @return 分页结果
     */
    ResultData<PageResult<McpResult>> pageMcpList(McpQuery query);

    /**
     * 查询MCP服务器详情
     * @param query 查询条件
     * @return 详情
     */
    ResultData<McpResult> queryMcpDetail(McpQuery query);

    /**
     * 测试MCP服务器连通性
     * @param mcpVO MCP服务器数据
     * @return 测试结果
     */
    ResultData<Void> testConnection(McpVO mcpVO);

}
