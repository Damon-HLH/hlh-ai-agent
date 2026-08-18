package com.hlh.hlhaiagent.agent;


import java.util.List;
import java.util.stream.Collectors;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.ai.tool.ToolCallback;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.hlh.hlhaiagent.agent.model.AgentState;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

/**
 * 处理工具调用的基础代理类，
 * 具有增强的抽象（实现了 think 和 act 方法，可以用于创建实例的父类）
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
public class ToolCallAgent extends ReActAgent {

    // 可用的工具
    private final ToolCallback[] availableTools;
    // 保存工具调用信息的响应结果（要调用哪些工具）
    private ChatResponse toolCallChatResponse;
    // 工具调用管理者
    private final ToolCallingManager toolCallingManager;
    // 禁用Spring AI 内置的工具调用机制，自己维护选项和消息上下文
    private final ChatOptions chatOptions;

    public ToolCallAgent(ToolCallback[] availableTools) {
        super();
        this.availableTools = availableTools;
        this.toolCallingManager = ToolCallingManager.builder().build();
        // 禁用 Spring AI 内置的工具调用机制，自己维护选项和消息上下文
        this.chatOptions = DashScopeChatOptions.builder()
                .internalToolExecutionEnabled(false)
                .build();
    }

    /**
     * 思考阶段：分析当前状态并确定要调用的工具
     *
     * @return 是否需要执行行动
     */
    @Override
    public boolean think() {
        // 1.拼接下一步(用户)提示词
        if (StrUtil.isNotBlank(getNextStepPrompt())) {
            UserMessage userMessage = new UserMessage(getNextStepPrompt());
            getMessageList().add(userMessage);
        }

        // 2.调用 AI 大模型，获取工具调用列表
        List<Message> messageList = getMessageList();
        Prompt prompt = new Prompt(messageList, chatOptions); // 把chatOptions传入Prompt
        try {
            ChatResponse chatResponse = getChatClient().prompt(prompt)
                    .system(getSystemPrompt())
                    .toolCallbacks(availableTools)
                    .call()
                    .chatResponse();
            //记录响应，用于等下Act
            this.toolCallChatResponse = chatResponse;

            // 3. 解析响应：包括工具调用结果，获取要调用的工具
            // 助手信息
            AssistantMessage assistantMessage = chatResponse.getResult().getOutput();
            // 获取要调用的工具列表
            List<AssistantMessage.ToolCall> toolCallList = assistantMessage.getToolCalls();

            // 输出提示信息
            String result = assistantMessage.getText();
            log.info(getName() + "的思考：" + result);  // agent的思考结果
            log.info(getName() + "选择了" + toolCallList.size() + "个工具");

            if (toolCallList.size() > 0) {
                String toolCallInfo = toolCallList.stream()
                        .map(toolCall ->
                                String.format("工具名称: %s, 参数: %s", toolCall.name(), toolCall.arguments())
                        ).collect(Collectors.joining("\n"));
                log.info(toolCallInfo);
            }

            // 如果不需要调用工具，返回false
            if (toolCallList.isEmpty()) {
                // 只有不调用工具时，才需要手动记录助手消息
                getMessageList().add(assistantMessage);
                return false;
            } else {
                // 需要调用工具时，无需记录助手消息，因为调用工具时会自动记录
                return true;
            }

        } catch (Exception e) {
            log.error(getName() + "的思考过程遇到了问题：" + e.getMessage());
            getMessageList().add(new AssistantMessage("处理时遇到了错误：" + e.getMessage()));
            return false;
        }
    }

    /**
     * 行动阶段：执行工具调用并处理结果
     *
     * @return 行动执行结果
     */
    @Override
    public String act() {
        if (!toolCallChatResponse.hasToolCalls()) {
            return "没有工具调用";
        }

        // 执行调用工具
        Prompt prompt = new Prompt(getMessageList(), chatOptions);
        ToolExecutionResult toolExecutionResult = toolCallingManager.executeToolCalls(prompt, toolCallChatResponse);

        // 更新消息上下文(toolCallingManager.executeToolCalls 源码逻辑里会自动拼接上下文)
        // conversationHistory已经包含了助手消息和工具调用返回的结果
        setMessageList(toolExecutionResult.conversationHistory());
        ToolResponseMessage response = (ToolResponseMessage) CollUtil.getLast(toolExecutionResult.conversationHistory());

        // 判断是否调用了终止工具
        boolean terminated = response.getResponses().stream()
                .anyMatch(r -> r.name().equals("doTerminate"));
        if (terminated) {
            //任务结束
            setState(AgentState.FINISHED);
        }

        // 格式化结果
        String results = response.getResponses().stream()
                .map(r -> "工具名称[" + r.name() + "]结果: " + r.responseData())
                .collect(Collectors.joining("\n"));
        log.info(results);

        return results;
    }

    /**
     * 任务结束后的最终总结：
     * 通过终止工具结束时，上下文中助手消息只包含工具调用指令，没有面向用户的回答，
     * 因此再发起一轮不带工具的调用，让模型基于已有上下文输出最终回复
     *
     * @return 最终面向用户的回答
     */
    @Override
    protected String finalizeResponse() {
        try {
            getMessageList().add(new UserMessage(
                    "任务已完成。请基于以上所有执行结果，直接给出面向用户的最终回答，不要再调用任何工具。"));
            ChatResponse finalResponse = getChatClient().prompt(
                            new Prompt(getMessageList(), chatOptions))
                    .system(getSystemPrompt())
                    .call()
                    .chatResponse();
            String finalText = finalResponse.getResult().getOutput().getText();
            getMessageList().add(finalResponse.getResult().getOutput());
            return finalText;
        } catch (Exception e) {
            log.error(getName() + "生成最终总结失败：" + e.getMessage());
            return null;
        }
    }
}
