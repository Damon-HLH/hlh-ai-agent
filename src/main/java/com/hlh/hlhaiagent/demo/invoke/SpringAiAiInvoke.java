package com.hlh.hlhaiagent.demo.invoke;


import jakarta.annotation.Resource;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Spring AI 框架调用 AI 大模型
 */
@Component  //在springboot 启动时 自动执行
public class SpringAiAiInvoke implements CommandLineRunner {

    //按名称注入
    @Resource
    private ChatModel dashscopeChatModel;

    @Override
    public void run(String... args) throws Exception {
        AssistantMessage assistantMessage = dashscopeChatModel.call(new Prompt("你好，我是hlh"))
                .getResult()
                .getOutput();
        System.out.println(assistantMessage.getText());
    }
}
