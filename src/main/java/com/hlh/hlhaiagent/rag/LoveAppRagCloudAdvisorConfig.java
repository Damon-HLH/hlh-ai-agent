package com.hlh.hlhaiagent.rag;


import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.rag.DashScopeDocumentRetriever;
import com.alibaba.cloud.ai.dashscope.rag.DashScopeDocumentRetrieverOptions;
import com.hlh.hlhaiagent.constant.KnowledgeIndex;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 自定义基于阿里云知识库服务的 RAG 增强顾问
 * 用于初始化基于阿里云知识库服务的 RAG 增强顾问 Bean
 */

@Slf4j
@Configuration
public class LoveAppRagCloudAdvisorConfig {

    //从配置文件读取阿里云百炼的 API 密钥
    @Value("${spring.ai.dashscope.api-key}")
    private String dashscopeApiKey;

    // 定义 Bean 方法，创建检索增强顾问
    @Bean
    public Advisor loveAppRagCloudAdvisor(){
        // 创建 DashScope API 客户端
        DashScopeApi dashScopeApi = DashScopeApi.builder().apiKey(dashscopeApiKey).build();
        // 知识库名称
        final String KNOWLEDGE_INDEX = KnowledgeIndex.LOVE_INDEX;
        // 创建文档检索器，连接指定知识库
        DashScopeDocumentRetriever dashScopeDocumentRetriever = new DashScopeDocumentRetriever(dashScopeApi,
                DashScopeDocumentRetrieverOptions.builder()
                        .withIndexName(KNOWLEDGE_INDEX)
                        .build());
        // 构建并返回检索增强顾问
        return RetrievalAugmentationAdvisor.builder()
                .documentRetriever(dashScopeDocumentRetriever)
                .build();
    }

}
