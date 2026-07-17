package com.hlh.hlhaiagent.rag;


import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;

/**
 * 创建自定义的 RAG RetrievalAugmentationAdvisor检索增强顾问 工厂
 * 允许空上下文：如果系统没搜到相关文档，则返回友好提示
 */
@Slf4j
public class LoveAppRagCustomAdvisorFactory {
    /**
     * 创建自定义的 RAG 检索增强顾问
     * @param vectorStore
     * @param status
     * @return
     */
    public static Advisor createLoveAppRagCustomAdvisor(VectorStore vectorStore, String status) {
        // 过滤特定状态的文档
        Filter.Expression expression = new FilterExpressionBuilder()
                .eq("status", status)
                .build();
        DocumentRetriever documentRetriever = VectorStoreDocumentRetriever.builder()
                .vectorStore(vectorStore)
                .filterExpression(expression) // 过滤条件
                .similarityThreshold(0.5) // 相似度阈值
                .topK(3) // 返回文档数量
                .build();
        return RetrievalAugmentationAdvisor.builder()
                .documentRetriever(documentRetriever)  //查询过滤
                .queryAugmenter(LoveAppContextualQueryAugmenterFactory.createInstance()) //上下文查询增强器：如果没搜到相关文档，则返回友好提示
                .build();
    }
}

