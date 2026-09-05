package com.spark.dao.llm;

import com.spark.common.bean.llm.entity.Agent;
import com.spark.common.bean.llm.query.AgentQuery;
import com.spark.common.bean.llm.result.AgentResult;
import com.spark.dao.BaseDao;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-05-17 10:12:34
 */
public interface AgentDao extends BaseDao<Agent> {

    /**
     * 插入数据
     * @param agent 数据对象
     * @return 影响行数
     */
    @Override
    int insert(Agent agent);

    /**
     * 删除数据
     * @param agent 数据对象
     * @return 影响行数
     */
    @Override
    int deleteById(Agent agent);

    /**
     * 修改数据
     * @param agent 数据对象
     * @return 影响行数
     */
    @Override
    int updateById(Agent agent);

    /**
     * 查询数量
     * @param query 查询条件
     * @return  数量
     */
    int queryAgentCount(AgentQuery query);

    /**
     * 查询列表
     * @param query 查询条件
     * @return 列表
     */
    List<AgentResult> queryAgentList(AgentQuery query);

    /**
     * 查询单条
     * @param query 查询条件
     * @return 单条数据
     */
    AgentResult queryAgent(AgentQuery query);

    /**
     * 查询最大ID
     * @return 最大ID
     */
    @Select("select max(id) from llm_agent")
    Long queryAgentMaxId();

    /**
     * 查询绑定指定技能的智能体ID列表
     * @param skillId 技能ID
     * @return 智能体ID列表
     */
    List<Long> queryAgentIdsBySkill(@Param("skillId") Long skillId);
}
