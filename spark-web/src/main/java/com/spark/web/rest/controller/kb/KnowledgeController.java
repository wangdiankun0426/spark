package com.spark.web.rest.controller.kb;

import com.spark.common.bean.base.PageResult;
import com.spark.common.bean.base.ResultData;
import com.spark.common.bean.kb.query.KnowledgeQuery;
import com.spark.common.bean.kb.query.RetrieveTestQuery;
import com.spark.common.bean.kb.result.KnowledgeResult;
import com.spark.common.bean.kb.result.RetrieveTestResult;
import com.spark.common.bean.kb.vo.KnowledgeVO;
import com.spark.kb.service.IKnowledgeService;
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
 * @since 2026-07-17 10:00:00
 */
@RestController
@RequestMapping("kb/knowledge")
public class KnowledgeController {
    @Autowired
    private IKnowledgeService knowledgeService;

    /**
     * 创建知识库
     * @param knowledgeVO 知识库参数
     * @return 响应结果
     */
    @PostMapping("create")
    public ResultData<Void> createKnowledge(KnowledgeVO knowledgeVO) {
        return knowledgeService.createKnowledge(knowledgeVO);
    }

    /**
     * 修改知识库
     * @param knowledgeVO 知识库参数
     * @return 响应结果
     */
    @PostMapping("update")
    public ResultData<Void> updateKnowledge(KnowledgeVO knowledgeVO) {
        return knowledgeService.updateKnowledge(knowledgeVO);
    }

    /**
     * 删除知识库
     * @param knowledgeVO 知识库参数
     * @return 响应结果
     */
    @PostMapping("delete")
    public ResultData<Void> deleteKnowledge(KnowledgeVO knowledgeVO) {
        return knowledgeService.deleteKnowledge(knowledgeVO);
    }

    /**
     * 分页查询知识库
     * @param query 查询参数
     * @return 响应结果
     */
    @GetMapping("pageList")
    public ResultData<PageResult<KnowledgeResult>> pageKnowledgeList(KnowledgeQuery query) {
        return knowledgeService.pageKnowledgeList(query);
    }

    /**
     * 查询知识库详情
     * @param query 查询参数
     * @return 响应结果
     */
    @GetMapping("detail")
    public ResultData<KnowledgeResult> queryKnowledgeDetail(KnowledgeQuery query) {
        return knowledgeService.queryKnowledgeDetail(query);
    }

    /**
     * 检索测试
     * @param query 检索测试参数
     * @return 响应结果
     */
    @GetMapping("testRetrieve")
    public ResultData<RetrieveTestResult> testRetrieve(RetrieveTestQuery query) {
        return knowledgeService.testRetrieve(query);
    }
}
