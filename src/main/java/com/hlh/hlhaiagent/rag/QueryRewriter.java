package com.hlh.hlhaiagent.rag;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer;
import org.springframework.stereotype.Component;
import org.springframework.ai.rag.preretrieval.query.transformation.QueryTransformer;

/**
 * 查询重写器 (将用户提示词进行重写，改写得更专业和凝练)
 * 提升问题的质量
 */
@Component
public class QueryRewriter {

    private final QueryTransformer queryTransformer;

    public QueryRewriter(ChatModel dashscopeChatModel) {
        ChatClient.Builder builder = ChatClient.builder(dashscopeChatModel);
        // 创建查询重写转换器
        queryTransformer = RewriteQueryTransformer.builder()
                .chatClientBuilder(builder)
                .build();
    }

    /**
     * 查询重写
     * @param prompt 用户输入的查询
     * @return
     */
    public String doQueryRewrite(String prompt) {
        Query query = new Query(prompt);
        // 执行查询重写
        Query transformedQuery = queryTransformer.transform(query);
        // 输出重写后的查询
        return transformedQuery.text();
    }
}

