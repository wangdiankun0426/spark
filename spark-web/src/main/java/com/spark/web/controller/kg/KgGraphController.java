package com.spark.web.controller.kg;

import com.spark.bean.base.PageResult;
import com.spark.bean.base.ResultData;
import com.spark.bean.kg.query.KgGraphQuery;
import com.spark.bean.kg.result.KgGraphResult;
import com.spark.bean.kg.vo.KgGraphVO;
import com.spark.kg.service.IKgGraphService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-07-22 17:30:00
 * 知识图谱管理
 */
@RestController
@RequestMapping("kg/graph")
public class KgGraphController {
    @Autowired
    private IKgGraphService kgGraphService;

    /**
     * 创建知识图谱
     * @param kgGraphVO 图谱参数
     * @return 响应结果
     */
    @PostMapping("create")
    public ResultData<Void> createKgGraph(KgGraphVO kgGraphVO) {
        return kgGraphService.createKgGraph(kgGraphVO);
    }

    /**
     * 修改知识图谱
     * @param kgGraphVO 图谱参数
     * @return 响应结果
     */
    @PostMapping("update")
    public ResultData<Void> updateKgGraph(KgGraphVO kgGraphVO) {
        return kgGraphService.updateKgGraph(kgGraphVO);
    }

    /**
     * 删除知识图谱
     * @param kgGraphVO 图谱参数
     * @return 响应结果
     */
    @PostMapping("delete")
    public ResultData<Void> deleteKgGraph(KgGraphVO kgGraphVO) {
        return kgGraphService.deleteKgGraph(kgGraphVO);
    }

    /**
     * 分页查询知识图谱
     * @param query 查询参数
     * @return 响应结果
     */
    @GetMapping("pageList")
    public ResultData<PageResult<KgGraphResult>> pageKgGraphList(KgGraphQuery query) {
        return kgGraphService.pageKgGraphList(query);
    }

    /**
     * 查询知识图谱详情
     * @param query 查询参数
     * @return 响应结果
     */
    @GetMapping("detail")
    public ResultData<KgGraphResult> queryKgGraphDetail(KgGraphQuery query) {
        return kgGraphService.queryKgGraphDetail(query);
    }

    /**
     * 按需展开节点关联子图
     * @param nodeId 节点 id
     * @param depth 扩展深度
     * @return 子图数据
     */
    @GetMapping("visual/expand")
    public ResultData<Map<String, Object>> expandNodeVisual(@RequestParam Long nodeId, @RequestParam(defaultValue = "1") int depth) {
        return kgGraphService.expandNodeVisual(nodeId, depth);
    }

    /**
     * 查询图谱统计信息
     * @param graphId 图谱 id
     * @return 统计信息
     */
    @GetMapping("visual/stats")
    public ResultData<Map<String, Object>> queryGraphStats(@RequestParam Long graphId) {
        return kgGraphService.queryGraphStats(graphId);
    }

    /**
     * 构建 GraphRAG 索引
     * @param graphId 图谱 id
     * @return 构建结果
     */
    @PostMapping("buildGraphRAG")
    public ResultData<Void> buildGraphRAGIndex(@RequestParam Long graphId) {
        return kgGraphService.buildGraphRAGIndex(graphId);
    }
}
