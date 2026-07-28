package com.spark.config.datasource;

import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2025/11/2 20:30
 * Flowable 引擎独立数据源配置
 * 从 spring.flowable.datasource.* 读取配置
 */
@Configuration
public class FlowableDataSourceConfig {

    @Bean(name = "flowableDataSource")
    @ConfigurationProperties(prefix = "spring.flowable.datasource")
    public DataSource flowableDataSource() {
        return DruidDataSourceBuilder.create().build();
    }

    @Bean(name = "flowableTransactionManager")
    public PlatformTransactionManager flowableTransactionManager(
            DataSource flowableDataSource) {
        return new DataSourceTransactionManager(flowableDataSource);
    }
}