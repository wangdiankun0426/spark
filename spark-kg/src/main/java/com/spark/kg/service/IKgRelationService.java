package com.spark.kg.service;

import com.spark.common.bean.base.PageResult;
import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.kg.query.KgRelationQuery;
import com.spark.common.bean.kg.result.KgRelationResult;
import com.spark.common.bean.kg.vo.KgRelationVO;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-07-22 16:00:00
 * 知识图谱关系服务接口
 */
public interface IKgRelationService {

    /**
     * 新增关系
     * @param kgRelationVO 关系数据
     * @return 新增结果
     */
    ResultData<Void> createKgRelation(KgRelationVO kgRelationVO);

    /**
     * 修改关系
     * @param kgRelationVO 关系数据
     * @return 修改结果
     */
    ResultData<Void> updateKgRelation(KgRelationVO kgRelationVO);

    /**
     * 删除关系
     * @param kgRelationVO 关系数据
     * @return 删除结果
     */
    ResultData<Void> deleteKgRelation(KgRelationVO kgRelationVO);

    /**
     * 分页查询关系
     * @param query 查询条件
     * @return 分页结果
     */
    ResultData<PageResult<KgRelationResult>> pageKgRelationList(KgRelationQuery query);

    /**
     * 查询关系详情
     * @param query 查询条件
     * @return 详情
     */
    ResultData<KgRelationResult> queryKgRelationDetail(KgRelationQuery query);
}
