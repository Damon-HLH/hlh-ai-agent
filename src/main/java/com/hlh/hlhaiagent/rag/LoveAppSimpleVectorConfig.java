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
 *
 * 【临时禁用说明】
 * 此配置类用于学习 RAG 本地向量库，启动时会加载文档并调用 DashScope Embedding API 产生费用。
 * 当前 AiController 中未使用 RAG 功能，为节省资源暂时禁用。
 * 如需重新启用：取消下方 @Configuration 的注释，同时恢复 LoveApp 中对应的 @Resource 字段和 RAG 方法即可。
 */
//@Configuration  // 临时禁用 RAG 本地向量库，避免启动时加载文档和调用 Embedding API 产生费用
public class LoveAppSimpleVectorConfig {

    @Resource
    private LoveAppDocumentLoader loveAppDocumentLoader;

    @Resource
    private MyKeyWordEnricher myKeyWordEnricher;

    //按需使用！ 初始化本地简易向量数据库，并保存切分好的文档
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