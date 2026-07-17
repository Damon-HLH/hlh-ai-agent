package com.hlh.hlhaiagent.demo.rag;


import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.preretrieval.query.expansion.MultiQueryExpander;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 多查询扩展 demo
 * 将 用户输入 通过 ai 扩充为多个不同表达的查询，便于搜索RAG知识库
 */
@Component
public class MultiQueryExpanderDemo {

    private final ChatClient.Builder chatClientBuilder;

    public MultiQueryExpanderDemo(ChatModel dashScopeChatModel) {
        this.chatClientBuilder = ChatClient.builder(dashScopeChatModel);
    }

    public List<Query> expend(String query){
        MultiQueryExpander queryExpander = MultiQueryExpander.builder()
                                            .chatClientBuilder(chatClientBuilder)
                                            .numberOfQueries(3)
                                            .build();
        List<Query> queries = queryExpander.expand(new Query(query));
        return queries;
    }
}
