package com.spark.web.rest.controller.kg;

import com.spark.common.bean.base.ResultData;
import com.spark.kg.service.IKgGraphService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-08-31 17:30:00
 * 知识图谱检索控制器
 */
@RestController
@RequestMapping("kg/search")
public class KgSearchController {
    @Autowired
    private IKgGraphService kgGraphService;

    /**
     * 图谱推理
     * @param question 用户问题
     * @param graphIds 图谱 id 列表
     * @return 推理结论
     */
    @GetMapping("reason")
    public ResultData<String> reason(@RequestParam String question, @RequestParam List<Long> graphIds) {
        return kgGraphService.reason(question, graphIds);
    }

    /**
     * 查询两个实体之间的最短路径
     * @param fromEntityId 起始实体 id
     * @param toEntityId 目标实体 id
     * @return 路径子图数据
     */
    @GetMapping("path")
    public ResultData<Map<String, Object>> findShortestPath(@RequestParam Long fromEntityId, @RequestParam Long toEntityId) {
        return kgGraphService.findShortestPath(fromEntityId, toEntityId);
    }

    /**
     * PageRank 算法
     * @param graphId 图谱 id
     * @param maxIterations 最大迭代次数
     * @return 分数结果
     */
    @GetMapping("pagerank")
    public ResultData<Map<Long, Double>> pageRank(@RequestParam Long graphId, @RequestParam(defaultValue = "20") int maxIterations) {
        return kgGraphService.pageRank(graphId, maxIterations);
    }

    /**
     * 中心度算法
     * @param graphId 图谱 id
     * @return 分数结果
     */
    @GetMapping("centrality")
    public ResultData<Map<Long, Double>> centrality(@RequestParam Long graphId) {
        return kgGraphService.centrality(graphId);
    }

    /**
     * 连通分量算法
     * @param graphId 图谱 id
     * @return 连通分量结果
     */
    @GetMapping("components")
    public ResultData<Map<Integer, List<Long>>> connectedComponents(@RequestParam Long graphId) {
        return kgGraphService.connectedComponents(graphId);
    }
}
