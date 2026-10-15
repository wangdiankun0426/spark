package com.spark.dao.llm;

import com.spark.common.bean.llm.entity.McpKey;
import com.spark.common.bean.llm.query.McpKeyQuery;
import com.spark.common.bean.llm.result.McpKeyResult;
import com.spark.dao.BaseDao;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-10-15 14:05:00
 */
public interface McpKeyDao extends BaseDao<McpKey> {

    /**
     * 插入数据
     * @param mcpKey 数据对象
     * @return 影响行数
     */
    @Override
    int insert(McpKey mcpKey);

    /**
     * 删除数据
     * @param mcpKey 数据对象
     * @return 影响行数
     */
    @Override
    int deleteById(McpKey mcpKey);

    /**
     * 修改数据
     * @param mcpKey 数据对象
     * @return 影响行数
     */
    @Override
    int updateById(McpKey mcpKey);

    /**
     * 查询列表
     * @param query 查询条件
     * @return 列表
     */
    List<McpKeyResult> queryMcpKeyList(McpKeyQuery query);

    /**
     * 查询单条
     * @param query 查询条件
     * @return 单条数据
     */
    McpKeyResult queryMcpKey(McpKeyQuery query);

    /**
     * 使用次数加一并刷新最近使用时间
     * @param id 主键
     * @return 影响行数
     */
    int incrUseCount(@Param("id") Long id);
}
