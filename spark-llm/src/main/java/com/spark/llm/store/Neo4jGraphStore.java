package com.spark.llm.store;

import com.spark.bean.kg.entity.KgEntity;
import com.spark.bean.kg.entity.RelationEdge;
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
     * @param name 实体名称（模糊匹配）
     * @return 实体节点列表
     */
    public List<KgEntity> queryEntityByName(Long graphId, String name) {
        List<KgEntity> list = new ArrayList<>();
        if (name == null || name.isBlank()) {
            return list;
        }
        // 优先使用全文索引（性能更好）
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
     * 使用 CONTAINS 查询实体（全文索引不可用时的回退方案）
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

}
