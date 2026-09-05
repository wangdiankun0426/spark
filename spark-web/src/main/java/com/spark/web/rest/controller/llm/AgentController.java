package com.spark.web.rest.controller.llm;

import com.spark.common.bean.llm.query.AgentQuery;
import com.spark.common.bean.llm.result.AgentResult;
import com.spark.common.bean.llm.vo.AgentVO;
import com.spark.common.bean.base.PageResult;
import com.spark.common.bean.base.ResultData;
import com.spark.llm.service.IAgentService;
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
 * @since 2026-05-16 21:23:13
 */
@RestController
@RequestMapping("llm/agent")
public class AgentController {

    @Autowired
    private IAgentService agentService;

    /**
     * 创建智能体
     * @param agentVO  创建智能体参数
     * @return 响应结果
     */
    @PostMapping("create")
    public ResultData<Void> createAgent(AgentVO agentVO) {
        return agentService.createAgent(agentVO);
    }

    /**
     * 修改智能体
     * @param agentVO 修改智能体参数
     * @return 响应结果
     */
    @PostMapping("update")
    public ResultData<Void> updateAgent(AgentVO agentVO) {
        return agentService.updateAgent(agentVO);
    }

    /**
     * 删除智能体
     * @param agentVO 删除智能体参数
     * @return 响应结果
     */
    @PostMapping("delete")
    public ResultData<Void> deleteAgent(AgentVO agentVO) {
        return agentService.deleteAgent(agentVO);
    }

    /**
     * 分页查询智能体
     * @param query 查询参数
     * @return 响应结果
     */
    @GetMapping("pageList")
    public ResultData<PageResult<AgentResult>> pageAgentList(AgentQuery query) {
        return agentService.pageAgentList(query);
    }

    /**
     * 查询智能体详情
     * @param query 查询参数
     * @return 响应结果
     */
    @GetMapping("detail")
    public ResultData<AgentResult> queryAgentDetail(AgentQuery query) {
        return agentService.queryAgentDetail(query);
    }
}
