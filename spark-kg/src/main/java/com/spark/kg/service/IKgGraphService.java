package com.spark.kg.service;

import com.spark.common.bean.base.PageResult;
import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.kg.query.KgGraphQuery;
import com.spark.common.bean.kg.result.KgGraphResult;
import com.spark.common.bean.kg.vo.KgGraphVO;

import java.util.List;
import java.util.Map;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-07-22 15:00:00
 * 知识图谱服务接口
 */
public interface IKgGraphService {

    /**
     * 创建知识图谱
     * @param kgGraphVO 图谱数据
     * @return 创建结果
     */
    ResultData<Void> createKgGraph(KgGraphVO kgGraphVO);

    /**
     * 修改知识图谱
     * @param kgGraphVO 图谱数据
     * @return 修改结果
     */
    ResultData<Void> updateKgGraph(KgGraphVO kgGraphVO);

    /**
     * 删除知识图谱
     * @param kgGraphVO 图谱数据
     * @return 删除结果
     */
    ResultData<Void> deleteKgGraph(KgGraphVO kgGraphVO);

    /**
     * 分页查询知识图谱
     * @param query 查询条件
     * @return 分页结果
     */
    ResultData<PageResult<KgGraphResult>> pageKgGraphList(KgGraphQuery query);

    /**
     * 查询知识图谱详情
     * @param query 查询条件
     * @return 详情
     */
    ResultData<KgGraphResult> queryKgGraphDetail(KgGraphQuery query);

    /**
     * 按需展开节点关联子图
     * @param nodeId 节点 id
     * @param depth 扩展深度
     * @return 子图数据
     */
    ResultData<Map<String, Object>> expandNodeVisual(Long nodeId, int depth);

    /**
     * 查询图谱统计信息
     * @param graphId 图谱 id
     * @return 统计信息
     */
    ResultData<Map<String, Object>> queryGraphStats(Long graphId);

    /**
     * 图谱推理
     * @param question 问题
     * @param graphIds 图谱 id 列表
     * @return 推理结果
     */
    ResultData<String> reason(String question, List<Long> graphIds);

    /**
     * 查询实体最短路径
     * @param fromEntityId 起始实体 id
     * @param toEntityId 目标实体 id
     * @return 路径数据
     */
    ResultData<Map<String, Object>> findShortestPath(Long fromEntityId, Long toEntityId);
}
