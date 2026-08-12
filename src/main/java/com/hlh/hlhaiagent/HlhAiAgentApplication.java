package com.hlh.hlhaiagent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

//排除 Spring AI 的 PgVectorStore 自动配置
//在你的启动类上排除自动配置，防止 Spring AI 再用 MySQL 的 JdbcTemplate 创建一个 pgVectorVectorStore：
//需要使用时，把 DataSourceAutoConfiguration.class 注释掉
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class HlhAiAgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(HlhAiAgentApplication.class, args);
    }

}
