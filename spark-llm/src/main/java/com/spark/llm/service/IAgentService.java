package com.spark.llm.service;

import com.spark.bean.llm.query.AgentQuery;
import com.spark.bean.llm.result.AgentResult;
import com.spark.bean.llm.vo.AgentVO;
import com.spark.bean.base.PageResult;
import com.spark.bean.base.ResultData;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-05-17 10:12:34
 */
public interface IAgentService {

    /**
     * 创建智能体
     * @param agentVO 智能体数据
     * @return 创建结果
     */
    ResultData<Void> createAgent(AgentVO agentVO);

    /**
     * 修改智能体
     * @param agentVO 智能体数据
     * @return 修改结果
     */
    ResultData<Void> updateAgent(AgentVO agentVO);

    /**
     * 删除智能体
     * @param agentVO 智能体数据
     * @return  删除结果
     */
    ResultData<Void> deleteAgent(AgentVO agentVO);

    /**
     * 分页查询智能体
     * @param query 查询智能体条件
     * @return 分页结果
     */
    ResultData<PageResult<AgentResult>> pageAgentList(AgentQuery query);

    /**
     * 查询智能体详情
     * @param query 查询智能体条件
     * @return 详情
     */
    ResultData<AgentResult> queryAgentDetail(AgentQuery query);

}
