package com.spark.web.rest.controller.llm;

import com.spark.common.bean.base.PageResult;
import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.llm.query.McpQuery;
import com.spark.common.bean.llm.result.McpResult;
import com.spark.common.bean.llm.vo.McpVO;
import com.spark.llm.service.IMcpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026/07/19 15:50:00
 */
@RestController
@RequestMapping("llm/mcp")
public class McpController {

    @Autowired
    private IMcpService mcpService;

    /**
     * 创建MCP服务器
     * @param mcpVO 创建参数
     * @return 响应结果
     */
    @PostMapping("create")
    public ResultData<Void> createMcp(McpVO mcpVO) {
        return mcpService.createMcp(mcpVO);
    }

    /**
     * 修改MCP服务器
     * @param mcpVO 修改参数
     * @return 响应结果
     */
    @PostMapping("update")
    public ResultData<Void> updateMcp(McpVO mcpVO) {
        return mcpService.updateMcp(mcpVO);
    }

    /**
     * 删除MCP服务器
     * @param mcpVO 删除参数
     * @return 响应结果
     */
    @PostMapping("delete")
    public ResultData<Void> deleteMcp(McpVO mcpVO) {
        return mcpService.deleteMcp(mcpVO);
    }

    /**
     * 分页查询MCP服务器
     * @param query 查询参数
     * @return 响应结果
     */
    @GetMapping("pageList")
    public ResultData<PageResult<McpResult>> pageMcpList(McpQuery query) {
        return mcpService.pageMcpList(query);
    }

    /**
     * 查询MCP服务器详情
     * @param query 查询参数
     * @return 响应结果
     */
    @GetMapping("detail")
    public ResultData<McpResult> queryMcpDetail(McpQuery query) {
        return mcpService.queryMcpDetail(query);
    }

    /**
     * 测试MCP服务器连通性
     * @param mcpVO 测试参数
     * @return 响应结果
     */
    @PostMapping("testConnection")
    public ResultData<Void> testConnection(McpVO mcpVO) {
        return mcpService.testConnection(mcpVO);
    }

}
