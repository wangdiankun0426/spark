package com.spark.kg.service;

import com.spark.common.bean.base.PageResult;
import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.kg.query.KgEntityQuery;
import com.spark.common.bean.kg.result.KgEntityResult;
import com.spark.common.bean.kg.vo.KgEntityVO;

import java.util.List;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-07-22 15:30:00
 * 知识图谱实体服务接口
 */
public interface IKgEntityService {

    /**
     * 新增实体
     * @param kgEntityVO 实体数据
     * @return 新增结果
     */
    ResultData<Void> createKgEntity(KgEntityVO kgEntityVO);

    /**
     * 修改实体
     * @param kgEntityVO 实体数据
     * @return 修改结果
     */
    ResultData<Void> updateKgEntity(KgEntityVO kgEntityVO);

    /**
     * 删除实体
     * @param kgEntityVO 实体数据
     * @return 删除结果
     */
    ResultData<Void> deleteKgEntity(KgEntityVO kgEntityVO);

    /**
     * 分页查询实体
     * @param query 查询条件
     * @return 分页结果
     */
    ResultData<PageResult<KgEntityResult>> pageKgEntityList(KgEntityQuery query);

    /**
     * 查询实体详情
     * @param query 查询条件
     * @return 详情
     */
    ResultData<KgEntityResult> queryKgEntityDetail(KgEntityQuery query);

    /**
     * 合并实体（消歧）
     * @param mainEntityId 主实体 id
     * @param mergedEntityIds 被合并的实体 id 列表
     * @return 合并结果
     */
    ResultData<Void> mergeKgEntity(Long mainEntityId, List<Long> mergedEntityIds);
}
