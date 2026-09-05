package com.spark.llm.store;

import com.spark.common.bean.kg.entity.KgEntity;
import com.spark.common.bean.kg.entity.RelationEdge;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-07-22 10:30:00
 * Neo4j 图存储实现，使用 Cypher 语句操作节点和边
 */
@Component
public class Neo4jGraphStore {
    private final static Logger logger = LoggerFactory.getLogger(Neo4jGraphStore.class);
    @Autowired
    private Driver neo4jDriver;
    /**
     * 节点标签
     */
    private static final String NODE_LABEL = "Entity";
    /**
     * 关系类型前缀（实际类型动态拼接）
     */
    private static final String RELATION_TYPE_PREFIX = "REL_";

    /**
     * 初始化时创建必要的索引和约束
     */
    @PostConstruct
    public void initIndexes() {
        try (Session session = neo4jDriver.session()) {
            // 1. 创建实体ID唯一约束
            session.run("CREATE CONSTRAINT entity_id_unique IF NOT EXISTS " +
                "FOR (e:Entity) REQUIRE e.id IS UNIQUE");
            logger.info("Neo4j constraint entity_id_unique initialized");

            // 2. 创建图谱ID索引
            session.run("CREATE INDEX entity_graphId_idx IF NOT EXISTS " +
                "FOR (e:Entity) ON (e.graphId)");
            logger.info("Neo4j index entity_graphId_idx initialized");

            // 3. 创建实体类型索引
            session.run("CREATE INDEX entity_type_idx IF NOT EXISTS " +
                "FOR (e:Entity) ON (e.type)");
            logger.info("Neo4j index entity_type_idx initialized");

            // 4. 创建实体名称索引
            session.run("CREATE INDEX entity_name_idx IF NOT EXISTS " +
                "FOR (e:Entity) ON (e.name)");
            logger.info("Neo4j index entity_name_idx initialized");

            // 5. 创建全文索引（支持名称和描述的全文搜索）
            session.run("CREATE FULLTEXT INDEX entity_fulltext IF NOT EXISTS " +
                "FOR (e:Entity) ON EACH [e.name, e.description]");
            logger.info("Neo4j fulltext index entity_fulltext initialized");

            logger.info("Neo4j indexes initialized successfully");
        } catch (Exception e) {
            logger.error("Failed to initialize Neo4j indexes", e);
        }
    }

    /**
     * 写入或更新实体节点（按 id MERGE）
     * @param node 实体节点
     * @return 写入的节点数量
     */
    public int upsertEntity(KgEntity node) {
        if (node == null || node.getId() == null) {
            return 0;
        }
        String cypher = "MERGE (n:" + NODE_LABEL + " {id: $id}) " +
                "SET n.name = $name, n.type = $type, n.description = $description, " +
                "n.graphId = $graphId";
        Map<String, Object> params = this.buildEntityParams(node);
        try (Session session = neo4jDriver.session()) {
            Result result = session.run(cypher, params);
            return 1;
        } catch (Exception e) {
            logger.error("写入实体节点失败, id={}, name={}", node.getId(), node.getName(), e);
            return 0;
        }
    }

    /**
     * 批量写入或更新实体节点
     * @param nodes 实体节点列表
     * @return 写入的节点数量
     */
    public int batchUpsertEntity(List<KgEntity> nodes) {
        if (nodes == null || nodes.isEmpty()) {
            return 0;
        }
        int success = 0;
        for (KgEntity node : nodes) {
            success += this.upsertEntity(node);
        }
        return success;
    }

    /**
     * 写入或更新关系边（按 id MERGE）
     * @param edge 关系边
     * @return 写入的边数量
     */
    public int upsertRelation(RelationEdge edge) {
        if (edge == null || edge.getId() == null || edge.getHeadEntityId() == null || edge.getTailEntityId() == null) {
            return 0;
        }
        String relationType = sanitizeRelationType(edge.getRelationType());
        String cypher = "MATCH (h:" + NODE_LABEL + " {id: $headId}), (t:" + NODE_LABEL + " {id: $tailId}) " +
                "MERGE (h)-[r:" + relationType + " {id: $id}]->(t) " +
                "SET r.graphId = $graphId, r.weight = $weight, r.relationType = $relationType";
        Map<String, Object> params = new HashMap<>();
        params.put("id", edge.getId());
        params.put("headId", edge.getHeadEntityId());
        params.put("tailId", edge.getTailEntityId());
        params.put("graphId", edge.getGraphId());
        params.put("weight", edge.getWeight() == null ? 1.0 : edge.getWeight());
        params.put("relationType", edge.getRelationType() == null ? "" : edge.getRelationType());
        try (Session session = neo4jDriver.session()) {
            session.run(cypher, params);
            return 1;
        } catch (Exception e) {
            logger.error("写入关系边失败, id={}, type={}", edge.getId(), edge.getRelationType(), e);
            return 0;
        }
    }

    /**
     * 批量写入或更新关系边
     * @param edges 关系边列表
     * @return 写入的边数量
     */
    public int batchUpsertRelation(List<RelationEdge> edges) {
        if (edges == null || edges.isEmpty()) {
            return 0;
        }
        int success = 0;
        for (RelationEdge edge : edges) {
            success += upsertRelation(edge);
        }
        return success;
    }

    /**
     * 按图谱 id 删除所有节点和边
     * @param graphId 图谱 id
     * @return 删除的数量
     */
    public int deleteByGraphId(Long graphId) {
        if (graphId == null) {
            return 0;
        }
        String cypher = "MATCH (n:" + NODE_LABEL + " {graphId: $graphId}) " +
                "DETACH DELETE n";
        try (Session session = neo4jDriver.session()) {
            Result result = session.run(cypher, Map.of("graphId", graphId));
            return result.consume().counters().nodesDeleted();
        } catch (Exception e) {
            logger.error("按图谱删除图数据失败, graphId={}", graphId, e);
            return 0;
        }
    }

    /**
     * 按实体 id 删除单个节点及其关联边
     * @param entityId 实体 id
     * @return 删除的数量
     */
    public int deleteByEntityId(Long entityId) {
        if (entityId == null) {
            return 0;
        }
        String cypher = "MATCH (n:" + NODE_LABEL + " {id: $entityId}) " +
                "DETACH DELETE n";
        try (Session session = neo4jDriver.session()) {
            Result result = session.run(cypher, Map.of("entityId", entityId));
            return result.consume().counters().nodesDeleted();
        } catch (Exception e) {
            logger.error("按实体删除图数据失败, entityId={}", entityId, e);
            return 0;
        }
    }

    /**
     * 迁移被合并实体的关系到主实体，并删除被合并节点
     * 将 old.Node 全部出边 (old)-(r)->(x) 与入边 (x)-(r)->(old) 改写为指向主实体，
     * 跳过由此产生的自环，最后删除旧节点，保证 Neo4j 与 MySQL 合并结果一致
     * @param mainEntityId 主实体 id
     * @param mergedEntityId 被合并实体 id
     * @return 处理的关系数 + 删除节点数
     */
    public int migrateRelations(Long mainEntityId, Long mergedEntityId) {
        if (mainEntityId == null || mergedEntityId == null) {
            return 0;
        }
        List<RelationEdge> edges = new ArrayList<>();
        String queryCypher = "MATCH (old:" + NODE_LABEL + " {id: $merged})-[r]-(x:" + NODE_LABEL + ") " +
                "RETURN r.id as id, r.graphId as graphId, type(r) as relType, " +
                "startNode(r).id as headId, endNode(r).id as tailId, " +
                "r.relationType as relationType, r.weight as weight";
        try (Session session = neo4jDriver.session()) {
            Result result = session.run(queryCypher, Map.of("merged", mergedEntityId));
            while (result.hasNext()) {
                Record record = result.next();
                RelationEdge edge = new RelationEdge();
                Value idValue = record.get("id");
                edge.setId(idValue.isNull() ? null : idValue.asLong());
                edge.setGraphId(record.get("graphId").asLong(0));
                edge.setHeadEntityId(record.get("headId").asLong());
                edge.setTailEntityId(record.get("tailId").asLong());
                Value relTypeValue = record.get("relationType");
                String relationType = relTypeValue.isNull() ? null : relTypeValue.asString(null);
                if (relationType == null || relationType.isEmpty()) {
                    relationType = record.get("relType").asString(null);
                }
                edge.setRelationType(relationType);
                Value weightValue = record.get("weight");
                edge.setWeight(weightValue.isNull() ? 1.0 : weightValue.asDouble(1.0));
                edges.add(edge);
            }
        } catch (Exception e) {
            logger.error("migrateRelations query error, merged={}", mergedEntityId, e);
            return 0;
        }
        int count = 0;
        for (RelationEdge edge : edges) {
            if (Objects.equals(edge.getHeadEntityId(), mergedEntityId)) {
                edge.setHeadEntityId(mainEntityId);
            }
            if (Objects.equals(edge.getTailEntityId(), mergedEntityId)) {
                edge.setTailEntityId(mainEntityId);
            }
            if (Objects.equals(edge.getHeadEntityId(), edge.getTailEntityId())) {
                continue;
            }
            count += this.upsertRelation(edge);
        }
        count += this.deleteByEntityId(mergedEntityId);
        return count;
    }

    /**
     * 按关系 id 删除单条边
     * @param relationId 关系 id
     * @return 删除的边数量
     */
    public int deleteByRelationId(Long relationId) {
        if (relationId == null) {
            return 0;
        }
        String cypher = "MATCH (h:" + NODE_LABEL + ")-[r]->(t:" + NODE_LABEL + ") " +
                "WHERE r.id = $relationId " +
                "DELETE r";
        try (Session session = neo4jDriver.session()) {
            Result result = session.run(cypher, Map.of("relationId", relationId));
            return result.consume().counters().relationshipsDeleted();
        } catch (Exception e) {
            logger.error("按关系删除图数据失败, relationId={}", relationId, e);
            return 0;
        }
    }

    /**
     * 查询实体的多跳邻居子图
     * @param entityId 起始实体 id
     * @param hopCount 跳数（1 或 2）
     * @return 子图节点和边（key: nodes / edges）
     */
    public Map<String, Object> querySubgraph(Long entityId, int hopCount) {
        Map<String, Object> subgraph = new HashMap<>();
        List<KgEntity> nodes = new ArrayList<>();
        List<RelationEdge> edges = new ArrayList<>();
        subgraph.put("nodes", nodes);
        subgraph.put("edges", edges);
        if (entityId == null) {
            return subgraph;
        }
        int depth = Math.max(1, Math.min(hopCount, 2));
        // 查询起始实体本身 + 邻居节点
        String nodeCypher = "MATCH (n:" + NODE_LABEL + " {id: $entityId})-[*0.." + depth + "]-(m:" + NODE_LABEL + ") " +
                "RETURN DISTINCT m.id as id, m.name as name, m.type as type, m.description as description, " +
                "m.graphId as graphId";
        try (Session session = neo4jDriver.session()) {
            Result nodeResult = session.run(nodeCypher, Map.of("entityId", entityId));
            while (nodeResult.hasNext()) {
                Record record = nodeResult.next();
                KgEntity node = new KgEntity();
                node.setId(record.get("id").asLong());
                node.setName(record.get("name").asString(null));
                node.setType(record.get("type").asString(null));
                node.setDescription(record.get("description").asString(null));
                node.setGraphId(record.get("graphId").asLong(0));
                nodes.add(node);
            }
            // 查询子图内节点之间的所有边（含 2 跳邻居之间的边）
            if (!nodes.isEmpty()) {
                List<Long> nodeIds = new ArrayList<>();
                for (KgEntity node : nodes) {
                    nodeIds.add(node.getId());
                }
                String edgeCypher = "MATCH (h:" + NODE_LABEL + ")-[r]->(t:" + NODE_LABEL + ") " +
                        "WHERE h.id IN $nodeIds AND t.id IN $nodeIds " +
                        "RETURN r.id as id, r.graphId as graphId, h.id as headId, t.id as tailId, " +
                        "r.relationType as relationType, type(r) as relType, r.weight as weight";
                Result edgeResult = session.run(edgeCypher, Map.of("nodeIds", nodeIds));
                while (edgeResult.hasNext()) {
                    Record record = edgeResult.next();
                    RelationEdge edge = new RelationEdge();
                    Value idValue = record.get("id");
                    edge.setId(idValue.isNull() ? null : idValue.asLong());
                    edge.setGraphId(record.get("graphId").asLong(0));
                    edge.setHeadEntityId(record.get("headId").asLong());
                    edge.setTailEntityId(record.get("tailId").asLong());
                    Value relationTypeValue = record.get("relationType");
                    String relationType = relationTypeValue.isNull() ? null : relationTypeValue.asString(null);
                    if (relationType == null || relationType.isEmpty()) {
                        relationType = record.get("relType").asString(null);
                    }
                    edge.setRelationType(relationType);
                    Value weightValue = record.get("weight");
                    edge.setWeight(weightValue.isNull() ? 1.0 : weightValue.asDouble(1.0));
                    edges.add(edge);
                }
            }
        } catch (Exception e) {
            logger.error("查询子图失败, entityId={}, hopCount={}", entityId, hopCount, e);
        }
        return subgraph;
    }

    /**
     * 按实体名称模糊查询实体节点
     * 优先使用全文索引，失败时回退到 CONTAINS 查询
     * @param graphId 图谱 id
     * @param name 实体名称
     * @return 实体节点列表
     */
    public List<KgEntity> queryEntityByName(Long graphId, String name) {
        List<KgEntity> list = new ArrayList<>();
        if (name == null || name.isBlank()) {
            return list;
        }
        // 优先使用全文索引
        String cypher = "CALL db.index.fulltext.queryNodes('entity_fulltext', $name) " +
                "YIELD node, score " +
                "WHERE node.graphId = $graphId " +
                "RETURN node.id as id, node.name as name, node.type as type, " +
                "node.description as description, node.graphId as graphId, score " +
                "ORDER BY score DESC " +
                "LIMIT 20";
        try (Session session = neo4jDriver.session()) {
            Result result = session.run(cypher, Map.of("graphId", graphId, "name", name));
            while (result.hasNext()) {
                Record record = result.next();
                KgEntity node = new KgEntity();
                node.setId(record.get("id").asLong());
                node.setName(record.get("name").asString(null));
                node.setType(record.get("type").asString(null));
                node.setDescription(record.get("description").asString(null));
                node.setGraphId(record.get("graphId").asLong(0));
                list.add(node);
            }
        } catch (Exception e) {
            logger.warn("全文索引查询失败，回退到CONTAINS查询, graphId={}, name={}", graphId, name, e);
            // 回退到 CONTAINS 查询
            list = queryEntityByNameContains(graphId, name);
        }
        return list;
    }

    /**
     * 使用 CONTAINS 查询实体，作为全文索引的回退方案
     * @param graphId 图谱 id
     * @param name 实体名称
     * @return 实体节点列表
     */
    private List<KgEntity> queryEntityByNameContains(Long graphId, String name) {
        List<KgEntity> list = new ArrayList<>();
        String cypher = "MATCH (n:" + NODE_LABEL + ") " +
                "WHERE n.graphId = $graphId AND n.name CONTAINS $name " +
                "RETURN n.id as id, n.name as name, n.type as type, n.description as description, " +
                "n.graphId as graphId " +
                "LIMIT 20";
        try (Session session = neo4jDriver.session()) {
            Result result = session.run(cypher, Map.of("graphId", graphId, "name", name));
            while (result.hasNext()) {
                Record record = result.next();
                KgEntity node = new KgEntity();
                node.setId(record.get("id").asLong());
                node.setName(record.get("name").asString(null));
                node.setType(record.get("type").asString(null));
                node.setDescription(record.get("description").asString(null));
                node.setGraphId(record.get("graphId").asLong(0));
                list.add(node);
            }
        } catch (Exception e) {
            logger.error("按名称查询实体失败, graphId={}, name={}", graphId, name, e);
        }
        return list;
    }

    /**
     * 查询两个实体之间的最短路径
     * @param fromEntityId 起始实体 id
     * @param toEntityId 目标实体 id
     * @return 路径子图数据
     */
    public Map<String, Object> findShortestPath(Long fromEntityId, Long toEntityId) {
        Map<String, Object> subgraph = new HashMap<>();
        List<KgEntity> nodes = new ArrayList<>();
        List<RelationEdge> edges = new ArrayList<>();
        subgraph.put("nodes", nodes);
        subgraph.put("edges", edges);
        if (fromEntityId == null || toEntityId == null || fromEntityId.equals(toEntityId)) {
            return subgraph;
        }
        String cypher = "MATCH path = shortestPath(" +
                "(from:" + NODE_LABEL + " {id: $fromId})-[*..5]-(to:" + NODE_LABEL + " {id: $toId})) " +
                "RETURN [n IN nodes(path) | n.id] AS nodeIds, " +
                "[n IN nodes(path) | n.name] AS nodeNames, " +
                "[n IN nodes(path) | n.type] AS nodeTypes, " +
                "[r IN relationships(path) | r.id] AS edgeIds, " +
                "[r IN relationships(path) | type(r)] AS edgeTypes";
        try (Session session = neo4jDriver.session()) {
            Result result = session.run(cypher, Map.of("fromId", fromEntityId, "toId", toEntityId));
            if (!result.hasNext()) {
                return subgraph;
            }
            Record record = result.next();
            List<Long> nodeIds = record.get("nodeIds").asList(Value::asLong);
            List<String> nodeNames = record.get("nodeNames").asList(Value::asString);
            List<String> nodeTypes = record.get("nodeTypes").asList(v -> v.asString(null));
            for (int i = 0; i < nodeIds.size(); i++) {
                KgEntity node = new KgEntity();
                node.setId(nodeIds.get(i));
                node.setName(i < nodeNames.size() ? nodeNames.get(i) : null);
                node.setType(i < nodeTypes.size() ? nodeTypes.get(i) : null);
                nodes.add(node);
            }
            List<Long> edgeIds = record.get("edgeIds").asList(v -> v.isNull() ? null : v.asLong());
            List<String> edgeTypes = record.get("edgeTypes").asList(Value::asString);
            for (int i = 0; i < edgeIds.size(); i++) {
                RelationEdge edge = new RelationEdge();
                if (edgeIds.get(i) != null) {
                    edge.setId(edgeIds.get(i));
                }
                edge.setHeadEntityId(nodeIds.get(i));
                edge.setTailEntityId(nodeIds.get(i + 1));
                edge.setRelationType(i < edgeTypes.size() ? edgeTypes.get(i) : null);
                edges.add(edge);
            }
        } catch (Exception e) {
            logger.error("findShortestPath error, fromId={}, toId={}", fromEntityId, toEntityId, e);
        }
        return subgraph;
    }

    /**
     * 校验图存储连接是否可用
     * @return true 表示连接正常
     */
    public boolean verifyConnect() {
        try (Session session = neo4jDriver.session()) {
            Result result = session.run("RETURN 1 AS num");
            return result.hasNext() && result.next().get("num").asInt() == 1;
        } catch (Exception e) {
            logger.error("Neo4j 连接校验失败", e);
            return false;
        }
    }

    /**
     * 构建实体节点参数
     * @param node 实体节点
     * @return 参数 map
     */
    private Map<String, Object> buildEntityParams(KgEntity node) {
        Map<String, Object> params = new HashMap<>();
        params.put("id", node.getId());
        params.put("name", node.getName() == null ? "" : node.getName());
        params.put("type", node.getType() == null ? "" : node.getType());
        params.put("description", node.getDescription() == null ? "" : node.getDescription());
        params.put("graphId", node.getGraphId() == null ? 0 : node.getGraphId());
        return params;
    }

    /**
     * 清洗关系类型，Neo4j 关系类型只能包含字母数字下划线
     * @param relationType 原始关系类型
     * @return 清洗后的关系类型
     */
    private String sanitizeRelationType(String relationType) {
        if (relationType == null || relationType.isBlank()) {
            return "REL_DEFAULT";
        }
        String sanitized = relationType.replaceAll("[^a-zA-Z0-9_]", "_").toUpperCase();
        return sanitized.isEmpty() ? "REL_DEFAULT" : RELATION_TYPE_PREFIX + sanitized;
    }

    /**
     * 投影 GDS 图
     * @param graphId 图谱 id
     * @param projectionName 投影图名称
     * @param undirected 是否无向
     */
    private void projectGraph(Long graphId, String projectionName, boolean undirected) {
        String orientation = undirected ? "UNDIRECTED" : "NATURAL";
        String cypher = "CALL gds.graph.project('" + projectionName + "', " +
                "'Entity', { " +
                "  ALL_RELATIONSHIPS: { orientation: '" + orientation + "' } " +
                "}, { nodeProperties: ['id', 'graphId'] })";
        try (Session session = neo4jDriver.session()) {
            session.run(cypher);
        }
    }

    /**
     * 删除 GDS 投影图
     * @param projectionName 投影图名称
     */
    private void dropProjection(String projectionName) {
        try (Session session = neo4jDriver.session()) {
            session.run("CALL gds.graph.drop('" + projectionName + "') YIELD graphName RETURN graphName");
        } catch (Exception e) {
            logger.warn("dropProjection fail, name={}", projectionName, e);
        }
    }

    /**
     * 按节点 id 扩展邻居子图
     * @param nodeId 起始节点 id
     * @param depth 扩展深度
     * @return 子图数据
     */
    public Map<String, Object> expandNode(Long nodeId, int depth) {
        Map<String, Object> subgraph = new HashMap<>();
        List<KgEntity> nodes = new ArrayList<>();
        List<RelationEdge> edges = new ArrayList<>();
        subgraph.put("nodes", nodes);
        subgraph.put("edges", edges);
        if (nodeId == null) {
            return subgraph;
        }
        int hop = Math.max(1, Math.min(depth, 2));
        String nodeCypher = "MATCH (n:" + NODE_LABEL + " {id: $nodeId})-[*0.." + hop + "]-(m:" + NODE_LABEL + ") " +
                "RETURN DISTINCT m.id as id, m.name as name, m.type as type, m.description as description, " +
                "m.graphId as graphId";
        try (Session session = neo4jDriver.session()) {
            Result nodeResult = session.run(nodeCypher, Map.of("nodeId", nodeId));
            while (nodeResult.hasNext()) {
                Record record = nodeResult.next();
                KgEntity node = new KgEntity();
                node.setId(record.get("id").asLong());
                node.setName(record.get("name").asString(null));
                node.setType(record.get("type").asString(null));
                node.setDescription(record.get("description").asString(null));
                node.setGraphId(record.get("graphId").asLong(0));
                nodes.add(node);
            }
            if (!nodes.isEmpty()) {
                List<Long> nodeIds = nodes.stream().map(KgEntity::getId).toList();
                String edgeCypher = "MATCH (h:" + NODE_LABEL + ")-[r]->(t:" + NODE_LABEL + ") " +
                        "WHERE h.id IN $nodeIds AND t.id IN $nodeIds " +
                        "RETURN r.id as id, h.id as headId, t.id as tailId, r.relationType as relationType, type(r) as relType";
                Result edgeResult = session.run(edgeCypher, Map.of("nodeIds", nodeIds));
                while (edgeResult.hasNext()) {
                    Record record = edgeResult.next();
                    RelationEdge edge = new RelationEdge();
                    Value idValue = record.get("id");
                    edge.setId(idValue.isNull() ? null : idValue.asLong());
                    edge.setHeadEntityId(record.get("headId").asLong());
                    edge.setTailEntityId(record.get("tailId").asLong());
                    Value relType = record.get("relationType");
                    String rt = relType.isNull() ? null : relType.asString(null);
                    if (rt == null || rt.isEmpty()) {
                        rt = record.get("relType").asString(null);
                    }
                    edge.setRelationType(rt);
                    edges.add(edge);
                }
            }
        } catch (Exception e) {
            logger.error("expandNode error, nodeId={}, depth={}", nodeId, depth, e);
        }
        return subgraph;
    }

    /**
     * 查询图谱统计信息
     * @param graphId 图谱 id
     * @return 统计信息
     */
    public Map<String, Object> queryGraphStats(Long graphId) {
        Map<String, Object> stats = new HashMap<>();
        if (graphId == null) {
            return stats;
        }
        String countCypher = "MATCH (n:" + NODE_LABEL + " {graphId: $graphId}) " +
                "RETURN count(n) AS nodeCount";
        String typeCypher = "MATCH (n:" + NODE_LABEL + " {graphId: $graphId}) " +
                "RETURN n.type AS type, count(n) AS cnt ORDER BY cnt DESC";
        String edgeCypher = "MATCH (n:" + NODE_LABEL + " {graphId: $graphId})-[r]->(m:" + NODE_LABEL + ") " +
                "WHERE m.graphId = $graphId " +
                "RETURN count(r) AS edgeCount";
        try (Session session = neo4jDriver.session()) {
            Result countResult = session.run(countCypher, Map.of("graphId", graphId));
            long nodeCount = countResult.hasNext() ? countResult.next().get("nodeCount").asLong() : 0;
            stats.put("nodeCount", nodeCount);

            Result edgeResult = session.run(edgeCypher, Map.of("graphId", graphId));
            long edgeCount = edgeResult.hasNext() ? edgeResult.next().get("edgeCount").asLong() : 0;
            stats.put("edgeCount", edgeCount);

            Result typeResult = session.run(typeCypher, Map.of("graphId", graphId));
            Map<String, Long> typeDistribution = new HashMap<>();
            while (typeResult.hasNext()) {
                Record record = typeResult.next();
                String type = record.get("type").asString(null);
                long cnt = record.get("cnt").asLong();
                typeDistribution.put(type == null || type.isEmpty() ? "未分类" : type, cnt);
            }
            stats.put("typeDistribution", typeDistribution);
        } catch (Exception e) {
            logger.error("queryGraphStats error, graphId={}", graphId, e);
        }
        return stats;
    }

    /**
     * 批量查询节点的关联度
     * @param entityIds 实体 id 列表
     * @return 关联度结果
     */
    public Map<Long, Integer> queryNodeDegrees(List<Long> entityIds) {
        Map<Long, Integer> degrees = new HashMap<>();
        if (entityIds == null || entityIds.isEmpty()) {
            return degrees;
        }
        String cypher = "MATCH (n:" + NODE_LABEL + ")-[r]-() " +
                "WHERE n.id IN $entityIds " +
                "RETURN n.id AS id, count(r) AS degree";
        try (Session session = neo4jDriver.session()) {
            Result result = session.run(cypher, Map.of("entityIds", entityIds));
            while (result.hasNext()) {
                Record record = result.next();
                degrees.put(record.get("id").asLong(), (int) record.get("degree").asLong());
            }
        } catch (Exception e) {
            logger.error("queryNodeDegrees error", e);
        }
        return degrees;
    }

    /**
     * GDS Louvain 社区检测
     * @param graphId 图谱 id
     * @return 社区划分结果
     */
    public Map<Integer, List<Long>> detectCommunities(Long graphId) {
        Map<Integer, List<Long>> communities = new HashMap<>();
        if (graphId == null) {
            return communities;
        }
        String projectionName = "kg-community-" + graphId;
        try {
            projectGraph(graphId, projectionName, true);
            String cypher = "CALL gds.louvain.stream('" + projectionName + "') " +
                    "YIELD nodeId, communityId " +
                    "RETURN gds.util.asNode(nodeId).id AS entityId, communityId";
            try (Session session = neo4jDriver.session()) {
                Result result = session.run(cypher);
                while (result.hasNext()) {
                    Record record = result.next();
                    Long entityId = record.get("entityId").asLong();
                    int communityId = (int) record.get("communityId").asLong();
                    communities.computeIfAbsent(communityId, k -> new ArrayList<>()).add(entityId);
                }
            }
        } catch (Exception e) {
            logger.error("detectCommunities error, graphId={}", graphId, e);
        } finally {
            dropProjection(projectionName);
        }
        return communities;
    }

    /**
     * GDS PageRank 算法
     * @param graphId 图谱 id
     * @param maxIterations 最大迭代次数
     * @return 分数结果
     */
    public Map<Long, Double> calculatePageRank(Long graphId, int maxIterations) {
        Map<Long, Double> scores = new HashMap<>();
        if (graphId == null) {
            return scores;
        }
        String projectionName = "kg-pagerank-" + graphId;
        try {
            projectGraph(graphId, projectionName, false);
            String cypher = "CALL gds.pageRank.stream('" + projectionName + "', { maxIterations: " + maxIterations + " }) " +
                    "YIELD nodeId, score " +
                    "RETURN gds.util.asNode(nodeId).id AS entityId, score";
            try (Session session = neo4jDriver.session()) {
                Result result = session.run(cypher);
                while (result.hasNext()) {
                    Record record = result.next();
                    scores.put(record.get("entityId").asLong(), record.get("score").asDouble());
                }
            }
        } catch (Exception e) {
            logger.error("calculatePageRank error, graphId={}", graphId, e);
        } finally {
            dropProjection(projectionName);
        }
        return scores;
    }

    /**
     * GDS 中心度算法
     * @param graphId 图谱 id
     * @return 分数结果
     */
    public Map<Long, Double> calculateCentrality(Long graphId) {
        Map<Long, Double> scores = new HashMap<>();
        if (graphId == null) {
            return scores;
        }
        String projectionName = "kg-centrality-" + graphId;
        try {
            projectGraph(graphId, projectionName, false);
            String cypher = "CALL gds.betweenness.stream('" + projectionName + "') " +
                    "YIELD nodeId, score " +
                    "RETURN gds.util.asNode(nodeId).id AS entityId, score";
            try (Session session = neo4jDriver.session()) {
                Result result = session.run(cypher);
                while (result.hasNext()) {
                    Record record = result.next();
                    scores.put(record.get("entityId").asLong(), record.get("score").asDouble());
                }
            }
        } catch (Exception e) {
            logger.error("calculateCentrality error, graphId={}", graphId, e);
        } finally {
            dropProjection(projectionName);
        }
        return scores;
    }

    /**
     * GDS 连通分量算法
     * @param graphId 图谱 id
     * @return 连通分量结果
     */
    public Map<Integer, List<Long>> findConnectedComponents(Long graphId) {
        Map<Integer, List<Long>> components = new HashMap<>();
        if (graphId == null) {
            return components;
        }
        String projectionName = "kg-wcc-" + graphId;
        try {
            projectGraph(graphId, projectionName, true);
            String cypher = "CALL gds.wcc.stream('" + projectionName + "') " +
                    "YIELD nodeId, componentId " +
                    "RETURN gds.util.asNode(nodeId).id AS entityId, componentId";
            try (Session session = neo4jDriver.session()) {
                Result result = session.run(cypher);
                while (result.hasNext()) {
                    Record record = result.next();
                    Long entityId = record.get("entityId").asLong();
                    int componentId = (int) record.get("componentId").asLong();
                    components.computeIfAbsent(componentId, k -> new ArrayList<>()).add(entityId);
                }
            }
        } catch (Exception e) {
            logger.error("findConnectedComponents error, graphId={}", graphId, e);
        } finally {
            dropProjection(projectionName);
        }
        return components;
    }

}
