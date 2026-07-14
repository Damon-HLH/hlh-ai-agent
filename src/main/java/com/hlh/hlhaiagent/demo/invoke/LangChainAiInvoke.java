package com.hlh.hlhaiagent.demo.invoke;


import dev.langchain4j.community.model.dashscope.QwenChatModel;

public class LangChainAiInvoke {

    public static void main(String[] args) {
        QwenChatModel qwenChatModel = QwenChatModel.builder()
                .apiKey(TestApiKey.API_KEY)
                .modelName("qwen-plus")
                .build();

        String chat = qwenChatModel.chat("你好，我是一个测试。你是谁？");
        System.out.println(chat);
    }
}
