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
}
