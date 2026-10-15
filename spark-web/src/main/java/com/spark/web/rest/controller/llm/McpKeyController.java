package com.spark.web.rest.controller.llm;

import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.llm.result.McpKeyResult;
import com.spark.common.bean.llm.vo.McpKeyVO;
import com.spark.llm.service.IMcpKeyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-10-15 14:35:00
 */
@RestController
@RequestMapping("llm/mcpKey")
public class McpKeyController {

    @Autowired
    private IMcpKeyService mcpKeyService;

    /**
     * 申请MCP密钥
     * @param mcpKeyVO 创建参数
     * @return 响应结果，data为生成的完整密钥
     */
    @PostMapping("create")
    public ResultData<String> createMcpKey(McpKeyVO mcpKeyVO) {
        return mcpKeyService.createMcpKey(mcpKeyVO);
    }

    /**
     * 删除MCP密钥
     * @param mcpKeyVO 删除参数
     * @return 响应结果
     */
    @PostMapping("delete")
    public ResultData<Void> deleteMcpKey(McpKeyVO mcpKeyVO) {
        return mcpKeyService.deleteMcpKey(mcpKeyVO);
    }

    /**
     * 查询当前用户的MCP密钥列表
     * @return 响应结果
     */
    @GetMapping("myList")
    public ResultData<List<McpKeyResult>> queryMyKeyList() {
        return mcpKeyService.queryMyKeyList();
    }

}
