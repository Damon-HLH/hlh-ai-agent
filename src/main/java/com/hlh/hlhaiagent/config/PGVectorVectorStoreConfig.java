package com.hlh.hlhaiagent.config;

import com.hlh.hlhaiagent.rag.LoveAppDocumentLoader;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.annotation.Resource;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import javax.sql.DataSource;
import static org.springframework.ai.vectorstore.pgvector.PgVectorStore.PgDistanceType.COSINE_DISTANCE;
import static org.springframework.ai.vectorstore.pgvector.PgVectorStore.PgIndexType.HNSW;

/**
 * 手动配置 pgvector 向量数据库
 */
@Configuration
public class PGVectorVectorStoreConfig {

    @Resource
    private LoveAppDocumentLoader loveAppDocumentLoader;

    @Value("${app.datasource.pgvector.url}")
    private String pgUrl;

    @Value("${app.datasource.pgvector.username}")
    private String pgUsername;

    @Value("${app.datasource.pgvector.password}")
    private String pgPassword;

    @Value("${app.datasource.pgvector.driver-class-name}")
    private String pgDriverClassName;

    @Bean
    public DataSource pgVectorDataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(pgUrl);
        dataSource.setUsername(pgUsername);
        dataSource.setPassword(pgPassword);
        dataSource.setDriverClassName(pgDriverClassName);
        dataSource.setMaximumPoolSize(5);
        dataSource.setMinimumIdle(1);
        return dataSource;
    }

    @Bean
    public JdbcTemplate pgVectorJdbcTemplate() {
        return new JdbcTemplate(pgVectorDataSource());
    }

    @Bean
    public VectorStore pgVectorVectorStore(EmbeddingModel dashScopeEmbeddingModel) {
        // 创建PgVectorStore实例，配置向量存储的参数
        VectorStore vectorStore = PgVectorStore.builder(pgVectorJdbcTemplate(), dashScopeEmbeddingModel)
                .dimensions(1024)                    // 设置向量的维度，可选，根据embedding的维度
                .distanceType(COSINE_DISTANCE)       // 设置计算向量间距离的方法，可选，默认为余弦距离
                .indexType(HNSW)                     // 设置索引类型，可选，默认为HNSW（高效近似最近邻搜索）
                .initializeSchema(true)              // 是否初始化数据库模式，可选，默认为false
                .schemaName("public")                // 设置数据库模式名称，可选，默认为"public"
                .vectorTableName("vector_store")     // 设置存储向量数据的表名，可选，默认为"vector_store"
                .maxDocumentBatchSize(10)         // 设置文档批量插入的最大数量，可选，默认为10000
                .build();

        // 下面这行按需调用，不然每次启动项目都会将本地md文件添加到向量数据库中
        // TODO:我已经加载过文档，所以不用每次再将文档重复加载到 PGVector中了！！！
        // 加载文档，分批添加（DashScope Embedding API 限制单次 batch size 不超过 10）
//        List<Document> documents = loveAppDocumentLoader.loadMarkDowns();
//        int batchSize = 10;
//        for (int i = 0; i < documents.size(); i += batchSize) {
//            int end = Math.min(i + batchSize, documents.size());
//            vectorStore.add(documents.subList(i, end));
//        }
        return vectorStore;
    }
}
