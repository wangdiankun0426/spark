package com.spark.kg.service;

import com.spark.bean.base.PageResult;
import com.spark.bean.base.ResultData;
import com.spark.bean.kg.query.KgGraphQuery;
import com.spark.bean.kg.result.KgGraphResult;
import com.spark.bean.kg.vo.KgGraphVO;

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
}
