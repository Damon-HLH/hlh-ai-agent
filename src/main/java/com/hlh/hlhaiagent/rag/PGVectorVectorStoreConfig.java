package com.hlh.hlhaiagent.rag;


import jakarta.annotation.Resource;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.springframework.ai.vectorstore.pgvector.PgVectorStore.PgDistanceType.COSINE_DISTANCE;
import static org.springframework.ai.vectorstore.pgvector.PgVectorStore.PgIndexType.HNSW;

@Configuration
public class PGVectorVectorStoreConfig {

    @Resource
    private LoveAppDocumentLoader loveAppDocumentLoader;

    @Bean
    public VectorStore pgVectorVectorStore(JdbcTemplate jdbcTemplate, EmbeddingModel dashScopeEmbeddingModel) {
        PgVectorStore vectorStore = PgVectorStore
                .builder(jdbcTemplate, dashScopeEmbeddingModel)
                .dimensions(1024)                    // Optional: defaults to model dimensions or 1536
                .distanceType(COSINE_DISTANCE)       // Optional: defaults to COSINE_DISTANCE
                .indexType(HNSW)                     // Optional: defaults to HNSW
                .initializeSchema(true)              // Optional: defaults to false
                .schemaName("public")                // Optional: defaults to "public"
                .vectorTableName("vector_store")     // Optional: defaults to "vector_store"
                .maxDocumentBatchSize(10)         // Optional: defaults to 10000
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
