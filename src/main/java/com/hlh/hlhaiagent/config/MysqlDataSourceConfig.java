package com.hlh.hlhaiagent.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import javax.swing.*;

/**
 * MYSQL数据库配置，与PGVector隔离开
 * MysqlDataSourceConfig.java  → MySQL @Primary DataSource + JdbcTemplate
 *                                       ↓
 *                            MyBatis-Plus / ChatMemoryMapper / 对话记忆
 *
 * PGVectorVectorStoreConfig.java → PostgreSQL DataSource + JdbcTemplate + VectorStore
 *                                       ↓
 *                                   PgVectorStore / RAG 知识库问答
 *
 * @Primary 的 MySQL JdbcTemplate 给 MyBatis-Plus 用（对话记忆），
 * 而 PgVectorStore 通过直接调用 @Bean方法精确拿到 PostgreSQL 的 JdbcTemplate。
 */

@Configuration
public class MysqlDataSourceConfig {

    @Value("${spring.datasource.url}")
    private String url;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Value("${spring.datasource.driver-class-name}")
    private String driverClassName;

    @Value("${spring.datasource.hikari.maximum-pool-size:10}")
    private int maxPoolSize;

    @Value("${spring.datasource.hikari.minimum-idle:2}")
    private int minIdle;

//    关键点：@Primary 让 MyBatis-Plus、Spring AI 等所有需要默认 DataSource 的组件自动走 MySQL；
//    只有 pgVectorVectorStore 通过 Bean 名称 pgVectorJdbcTemplate 精确注入 PostgreSQL 的 JdbcTemplate。
//    这样 @Primary 的 MySQL JdbcTemplate 给 MyBatis-Plus 用（对话记忆），而 PgVectorStore 通过直接调用 @Bean 方法精确拿到 PostgreSQL 的 JdbcTemplate。
    @Primary
    @Bean
    public DataSource dataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        dataSource.setDriverClassName(driverClassName);
        dataSource.setMaximumPoolSize(maxPoolSize);
        dataSource.setMinimumIdle(minIdle);
        return dataSource;
    }

    @Primary
    @Bean
    public JdbcTemplate jdbcTemplate() {
        return new JdbcTemplate(dataSource());
    }
}