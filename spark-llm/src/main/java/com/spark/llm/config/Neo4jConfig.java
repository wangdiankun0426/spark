package com.spark.llm.config;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2026-07-22 10:00:00
 * Neo4j 图数据库配置
 */
@Configuration
public class Neo4jConfig {

    private final static Logger logger = LoggerFactory.getLogger(Neo4jConfig.class);

    @Value("${spring.neo4j.uri:bolt://localhost:7687}")
    private String uri;

    @Value("${spring.neo4j.username:neo4j}")
    private String username;

    @Value("${spring.neo4j.password:123456}")
    private String password;

    /**
     * 创建 Neo4j Driver Bean
     * @return Neo4j Driver 实例
     */
    @Bean(destroyMethod = "close")
    public Driver neo4jDriver() {
        logger.info("初始化 Neo4j Driver, uri: {}", uri);
        return GraphDatabase.driver(uri, AuthTokens.basic(username, password));
    }

}
