package com.hlh.hlhaiagent.agent;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Component;

import com.hlh.hlhaiagent.advisor.MyLoggerAdvisor;

/**
 * hlh的超级智能体，具备自主规划能力
 */
@Component
public class HlhManus extends ToolCallAgent {


    public HlhManus(ToolCallback[] allTools, ChatModel dashscopeChatModel /*,ToolCallbackProvider toolCallbackProvider*/) {
        super(allTools);

        this.setName("hlhManus");
        this.setMaxSteps(20); //最大执行步骤

        // 提示词设置（能力全面的AI助手，旨在解决用户的任何问题）
        String SYSTEM_PROMPT = """  
                You are hlhManus, an all-capable AI assistant, aimed at solving any task presented by the user.  
                You have various tools at your disposal that you can call upon to efficiently complete complex requests.  
                Language rule: Always reply to the user in the same language as the user's message (Chinese by default).  
                Document rule: When generating documents (PDF, email, files, etc.), write the content in Chinese (简体中文) by default, \
                unless the user explicitly requests another language. Never use emoji characters in generated documents.  
                """;
        this.setSystemPrompt(SYSTEM_PROMPT);

        String NEXT_STEP_PROMPT = """  
                Based on user needs, proactively select the most appropriate tool or combination of tools.  
                For complex tasks, you can break down the problem and use different tools step by step to solve it.  
                After using each tool, clearly explain the execution results and suggest the next steps.  
                If the user's question can be answered directly without any tool, just answer it directly and finish the task.  
                If you want to stop the interaction at any point, use the `terminate` tool/function call.  
                """;
        this.setNextStepPrompt(NEXT_STEP_PROMPT);

        // 初始化AI对话客户端
        ChatClient chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultAdvisors(new MyLoggerAdvisor())
//                .defaultToolCallbacks(toolCallbackProvider)  // 注册远程 MCP 服务
                .build();
        this.setChatClient(chatClient);
    }
}
