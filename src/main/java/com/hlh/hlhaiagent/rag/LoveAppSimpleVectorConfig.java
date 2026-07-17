package com.hlh.hlhaiagent.rag;


import jakarta.annotation.Resource;
import kotlin.reflect.KVariance;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.List;

/**
 * 恋爱大师向量数据库配置（初始化基于内存的向量数据库Bean）
 * 构造一个 VectorStore 对象，用于存储和检索向量数据
 */
@Configuration
public class LoveAppSimpleVectorConfig {

    @Resource
    private LoveAppDocumentLoader loveAppDocumentLoader;

    @Resource
    private MyKeyWordEnricher myKeyWordEnricher;

    //初始化向量数据库，并保存切分好的文档
    @Bean
    VectorStore loveAppVectorStore(EmbeddingModel dashScopeEmbeddingModel) {
        //创建本地简易向量数据库
        SimpleVectorStore simpleVectorStore = SimpleVectorStore.builder(dashScopeEmbeddingModel).build();
        //调用文档加载器 得到划分好的文档chunks
        List<Document> documents = loveAppDocumentLoader.loadMarkDowns();
        //自动补充元信息
        List<Document> enrichedDocuments = myKeyWordEnricher.enrichDocuments(documents);
        simpleVectorStore.add(enrichedDocuments);
        return simpleVectorStore;
    }
}