package com.spark.utils;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;

import java.util.*;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-08-11 10:00:00
 * DAG合法性校验工具
 */
public class DagUtil {

    /**
     * 校验DAG JSON合法性
     * @param dagJson DAG JSON字符串
     * @return 校验失败返回错误描述，成功返回null
     */
    public static String validate(String dagJson) {
        if (dagJson == null || dagJson.trim().isEmpty()) {
            return "DAG数据不能为空";
        }
        JSONObject dag;
        try {
            dag = JSON.parseObject(dagJson);
        } catch (Exception e) {
            return "DAG JSON格式非法";
        }
        JSONArray nodes = dag.getJSONArray("nodes");
        JSONArray sequences = dag.getJSONArray("sequences");
        if (nodes == null || nodes.isEmpty()) {
            return "DAG必须包含至少一个节点";
        }
        if (sequences == null) {
            sequences = new JSONArray();
        }

        // 1. 校验有且仅有一个 start 节点
        long startCount = nodes.stream().filter(n -> "start".equals(((JSONObject) n).getString("type"))).count();
        if (startCount == 0) {
            return "DAG缺少开始节点（start）";
        }
        if (startCount > 1) {
            return "DAG只能有一个开始节点（start）";
        }

        // 2. 校验至少有一个 end 节点
        long endCount = nodes.stream().filter(n -> "end".equals(((JSONObject) n).getString("type"))).count();
        if (endCount == 0) {
            return "DAG缺少结束节点（end）";
        }

        // 3. 收集所有节点ID
        Set<String> nodeIds = new HashSet<>();
        for (int i = 0; i < nodes.size(); i++) {
            JSONObject node = nodes.getJSONObject(i);
            String nodeId = node.getString("id");
            if (nodeId == null || nodeId.trim().isEmpty()) {
                return "存在节点缺少id";
            }
            if (!nodeIds.add(nodeId)) {
                return "节点id重复：" + nodeId;
            }
        }

        // 4. 校验连线引用的节点存在
        for (int i = 0; i < sequences.size(); i++) {
            JSONObject edge = sequences.getJSONObject(i);
            String source = edge.getString("source");
            String target = edge.getString("target");
            if (source == null || !nodeIds.contains(source)) {
                return "连线引用了不存在的源节点：" + source;
            }
            if (target == null || !nodeIds.contains(target)) {
                return "连线引用了不存在目标节点：" + target;
            }
        }

        // 5. 校验无孤立节点（非start节点至少有一个入边）
        Set<String> hasIncoming = new HashSet<>();
        for (int i = 0; i < sequences.size(); i++) {
            JSONObject edge = sequences.getJSONObject(i);
            hasIncoming.add(edge.getString("target"));
        }
        for (int i = 0; i < nodes.size(); i++) {
            JSONObject node = nodes.getJSONObject(i);
            String nodeId = node.getString("id");
            String nodeType = node.getString("type");
            if (!"start".equals(nodeType) && !hasIncoming.contains(nodeId)) {
                return "节点【" + node.getString("name") + "】缺少入边（孤立节点）";
            }
        }

        // 6. 校验无环（拓扑排序）
        Map<String, List<String>> adjacency = new HashMap<>();
        Map<String, Integer> inDegree = new HashMap<>();
        for (String nid : nodeIds) {
            adjacency.put(nid, new ArrayList<>());
            inDegree.put(nid, 0);
        }
        for (int i = 0; i < sequences.size(); i++) {
            JSONObject edge = sequences.getJSONObject(i);
            String source = edge.getString("source");
            String target = edge.getString("target");
            adjacency.get(source).add(target);
            inDegree.put(target, inDegree.get(target) + 1);
        }
        Queue<String> queue = new LinkedList<>();
        for (Map.Entry<String, Integer> entry : inDegree.entrySet()) {
            if (entry.getValue() == 0) {
                queue.add(entry.getKey());
            }
        }
        int visitedCount = 0;
        while (!queue.isEmpty()) {
            String current = queue.poll();
            visitedCount++;
            for (String neighbor : adjacency.get(current)) {
                int newDegree = inDegree.get(neighbor) - 1;
                inDegree.put(neighbor, newDegree);
                if (newDegree == 0) {
                    queue.add(neighbor);
                }
            }
        }
        if (visitedCount != nodeIds.size()) {
            return "DAG存在环路，请检查连线";
        }

        // 7. 校验condition节点的出边分支覆盖
        for (int i = 0; i < nodes.size(); i++) {
            JSONObject node = nodes.getJSONObject(i);
            if ("condition".equals(node.getString("type"))) {
                String nodeId = node.getString("id");
                long outEdgeCount = sequences.stream().filter(e -> nodeId.equals(((JSONObject) e).getString("source"))).count();
                if (outEdgeCount < 2) {
                    return "条件分支节点【" + node.getString("name") + "】至少需要两条出边（含else分支）";
                }
            }
        }

        return null;
    }

}
