package com.hlh.hlhaiagent.agent;

import java.util.List;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;

import com.hlh.hlhaiagent.agent.model.AgentState;

import cn.hutool.core.util.StrUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

/**
 * ReAct(Reasoning and Acting)模式代理抽象类
 * 实现思考-行动循环
 */
@EqualsAndHashCode(callSuper = true)
@Slf4j
@Data
public abstract class ReActAgent extends BaseAgent {


    /**
     * 思考决策阶段
     *
     * @return 是否需要执行行动
     */
    public abstract boolean think();

    /**
     * 执行行动阶段
     *
     * @return 行动执行结果
     */
    public abstract String act();

    /**
     * 获取最后一条助手消息的文本内容（即模型最近一次生成的回答）
     *
     * @return 助手消息文本，不存在时返回 null
     */
    protected String getLastAssistantText() {
        List<Message> messageList = getMessageList();
        for (int i = messageList.size() - 1; i >= 0; i--) {
            if (messageList.get(i) instanceof AssistantMessage assistantMessage) {
                String text = assistantMessage.getText();
                if (StrUtil.isNotBlank(text)) {
                    return text;
                }
            }
        }
        return null;
    }

    /**
     * 任务结束后的最终总结（默认不做额外处理，子类可覆写）
     *
     * @return 最终面向用户的回答，返回空则沿用 act 的结果
     */
    protected String finalizeResponse() {
        return null;
    }

    /**
     * 定义单个步骤：思考和行动
     *
     * @return 步骤执行结果
     */
    @Override
    public String step() {
        try {
            // 先思考后行动
            boolean shouldAct = think();
            if (!shouldAct) {
                // think 阶段未选择任何工具，说明模型已直接给出最终回答（或无法继续推进），任务到此结束
                setState(AgentState.FINISHED);
                // 取回模型本轮生成的回答文本作为步骤结果输出，避免丢失 AI 的真实回复
                String answer = getLastAssistantText();
                return StrUtil.isNotBlank(answer) ? answer : "思考完成 - 无需行动";
            }
            String result = act();
            // 若 act 中调用了终止工具，状态已变为 FINISHED，此时补充一轮面向用户的最终总结
            if (getState() == AgentState.FINISHED) {
                String finalResult = finalizeResponse();
                if (StrUtil.isNotBlank(finalResult)) {
                    result = finalResult;
                }
            }
            return result;
        } catch (Exception e) {
            log.error("步骤执行失败", e);
            return "步骤执行失败: " + e.getMessage();
        }
    }
}
