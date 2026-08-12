package com.hlh.hlhaiagent.agent;

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
                return "思考完成 - 无需行动";
            }
            return act();
        } catch (Exception e) {
            log.error("步骤执行失败", e);
            return "步骤执行失败: " + e.getMessage();
        }
    }
}
