package com.spark.web.rest.controller.kg;

import com.spark.common.bean.base.PageResult;
import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.kg.query.KgEntityQuery;
import com.spark.common.bean.kg.result.KgEntityResult;
import com.spark.common.bean.kg.vo.KgEntityVO;
import com.spark.kg.service.IKgEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-07-22 17:30:00
 * 知识图谱实体管理
 */
@RestController
@RequestMapping("kg/entity")
public class KgEntityController {
    @Autowired
    private IKgEntityService kgEntityService;

    /**
     * 新增实体
     * @param kgEntityVO 实体参数
     * @return 响应结果
     */
    @PostMapping("create")
    public ResultData<Void> createKgEntity(KgEntityVO kgEntityVO) {
        return kgEntityService.createKgEntity(kgEntityVO);
    }

    /**
     * 修改实体
     * @param kgEntityVO 实体参数
     * @return 响应结果
     */
    @PostMapping("update")
    public ResultData<Void> updateKgEntity(KgEntityVO kgEntityVO) {
        return kgEntityService.updateKgEntity(kgEntityVO);
    }

    /**
     * 删除实体
     * @param kgEntityVO 实体参数
     * @return 响应结果
     */
    @PostMapping("delete")
    public ResultData<Void> deleteKgEntity(KgEntityVO kgEntityVO) {
        return kgEntityService.deleteKgEntity(kgEntityVO);
    }

    /**
     * 分页查询实体
     * @param query 查询参数
     * @return 响应结果
     */
    @GetMapping("pageList")
    public ResultData<PageResult<KgEntityResult>> pageKgEntityList(KgEntityQuery query) {
        return kgEntityService.pageKgEntityList(query);
    }

    /**
     * 查询实体详情
     * @param query 查询参数
     * @return 响应结果
     */
    @GetMapping("detail")
    public ResultData<KgEntityResult> queryKgEntityDetail(KgEntityQuery query) {
        return kgEntityService.queryKgEntityDetail(query);
    }

    /**
     * 合并实体（消歧）
     * @param mainEntityId 主实体 id
     * @param mergedEntityIds 被合并的实体 id 列表
     * @return 响应结果
     */
    @PostMapping("merge")
    public ResultData<Void> mergeKgEntity(@RequestParam Long mainEntityId, @RequestParam List<Long> mergedEntityIds) {
        return kgEntityService.mergeKgEntity(mainEntityId, mergedEntityIds);
    }

    /**
     * 审核实体
     * @param entityId 实体ID
     * @param auditStatus 审核状态
     * @return 响应结果
     */
    @PostMapping("audit")
    public ResultData<Void> auditKgEntity(@RequestParam Long entityId, @RequestParam Integer auditStatus) {
        return kgEntityService.auditKgEntity(entityId, auditStatus);
    }

    /**
     * 批量审核实体
     * @param entityIds 实体ID列表
     * @param auditStatus 审核状态
     * @return 响应结果
     */
    @PostMapping("batchAudit")
    public ResultData<Void> batchAuditKgEntity(@RequestParam List<Long> entityIds, @RequestParam Integer auditStatus) {
        return kgEntityService.batchAuditKgEntity(entityIds, auditStatus);
    }
}
