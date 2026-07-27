package com.spark.web.controller.kg;

import com.spark.bean.base.PageResult;
import com.spark.bean.base.ResultData;
import com.spark.bean.kg.query.KgRelationQuery;
import com.spark.bean.kg.result.KgRelationResult;
import com.spark.bean.kg.vo.KgRelationVO;
import com.spark.kg.service.IKgRelationService;
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
 * @since 2026-07-22 17:30:00
 * 知识图谱关系管理
 */
@RestController
@RequestMapping("kg/relation")
public class KgRelationController {
    @Autowired
    private IKgRelationService kgRelationService;

    /**
     * 新增关系
     * @param kgRelationVO 关系参数
     * @return 响应结果
     */
    @PostMapping("create")
    public ResultData<Void> createKgRelation(KgRelationVO kgRelationVO) {
        return kgRelationService.createKgRelation(kgRelationVO);
    }

    /**
     * 修改关系
     * @param kgRelationVO 关系参数
     * @return 响应结果
     */
    @PostMapping("update")
    public ResultData<Void> updateKgRelation(KgRelationVO kgRelationVO) {
        return kgRelationService.updateKgRelation(kgRelationVO);
    }

    /**
     * 删除关系
     * @param kgRelationVO 关系参数
     * @return 响应结果
     */
    @PostMapping("delete")
    public ResultData<Void> deleteKgRelation(KgRelationVO kgRelationVO) {
        return kgRelationService.deleteKgRelation(kgRelationVO);
    }

    /**
     * 分页查询关系
     * @param query 查询参数
     * @return 响应结果
     */
    @GetMapping("pageList")
    public ResultData<PageResult<KgRelationResult>> pageKgRelationList(KgRelationQuery query) {
        return kgRelationService.pageKgRelationList(query);
    }

    /**
     * 查询关系详情
     * @param query 查询参数
     * @return 响应结果
     */
    @GetMapping("detail")
    public ResultData<KgRelationResult> queryKgRelationDetail(KgRelationQuery query) {
        return kgRelationService.queryKgRelationDetail(query);
    }
}
