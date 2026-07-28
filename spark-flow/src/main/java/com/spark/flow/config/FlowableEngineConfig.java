package com.spark.flow.config;

import org.flowable.common.engine.impl.history.HistoryLevel;
import org.flowable.engine.*;
import org.flowable.spring.SpringProcessEngineConfiguration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2025/11/2 20:00
 * Flowable 引擎配置
 * 数据源和事务管理器由 spark-config 模块的 FlowableDataSourceConfig 提供
 */
@Configuration
public class FlowableEngineConfig {

    /**
     * Flowable 引擎配置
     */
    @Bean
    public SpringProcessEngineConfiguration springProcessEngineConfiguration(
            @Qualifier("flowableDataSource") DataSource dataSource,
            @Qualifier("flowableTransactionManager") PlatformTransactionManager transactionManager) {
        SpringProcessEngineConfiguration config = new SpringProcessEngineConfiguration();
        config.setDataSource(dataSource);
        config.setTransactionManager(transactionManager);
        config.setDatabaseSchemaUpdate("true");
        config.setDbHistoryUsed(true);
        config.setHistoryLevel(HistoryLevel.AUDIT);
        config.setAsyncExecutorActivate(true);
        config.setDisableIdmEngine(true);
        config.setDisableEventRegistry(true);
        config.setDatabaseType("mysql");
        config.setJdbcMaxActiveConnections(20);
        config.setJdbcMaxIdleConnections(10);
        config.setJdbcMaxCheckoutTime(60000);
        config.setJdbcMaxWaitTime(60000);
        return config;
    }

    /**
     * ProcessEngine Bean
     */
    @Bean
    public ProcessEngine processEngine(SpringProcessEngineConfiguration configuration) {
        return configuration.buildProcessEngine();
    }

    /**
     * RepositoryService Bean
     */
    @Bean
    public RepositoryService repositoryService(ProcessEngine processEngine) {
        return processEngine.getRepositoryService();
    }

    /**
     * RuntimeService Bean
     */
    @Bean
    public RuntimeService runtimeService(ProcessEngine processEngine) {
        return processEngine.getRuntimeService();
    }

    /**
     * TaskService Bean
     */
    @Bean
    public TaskService taskService(ProcessEngine processEngine) {
        return processEngine.getTaskService();
    }

    /**
     * HistoryService Bean
     */
    @Bean
    public HistoryService historyService(ProcessEngine processEngine) {
        return processEngine.getHistoryService();
    }
}