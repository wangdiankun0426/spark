package com.spark.dao.llm;

import com.spark.common.bean.llm.entity.Mcp;
import com.spark.common.bean.llm.query.McpQuery;
import com.spark.common.bean.llm.result.McpResult;
import com.spark.dao.BaseDao;

import java.util.List;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026/07/19 15:20:00
 */
public interface McpDao extends BaseDao<Mcp> {

    /**
     * 插入数据
     * @param mcp 数据对象
     * @return 影响行数
     */
    @Override
    int insert(Mcp mcp);

    /**
     * 删除数据
     * @param mcp 数据对象
     * @return 影响行数
     */
    @Override
    int deleteById(Mcp mcp);

    /**
     * 修改数据
     * @param mcp 数据对象
     * @return 影响行数
     */
    @Override
    int updateById(Mcp mcp);

    /**
     * 查询数量
     * @param query 查询条件
     * @return 数量
     */
    int queryMcpCount(McpQuery query);

    /**
     * 查询列表
     * @param query 查询条件
     * @return 列表
     */
    List<McpResult> queryMcpList(McpQuery query);

    /**
     * 查询单条
     * @param query 查询条件
     * @return 单条数据
     */
    McpResult queryMcp(McpQuery query);
}
