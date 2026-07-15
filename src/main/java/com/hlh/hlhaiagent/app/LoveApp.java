package com.hlh.hlhaiagent.app;



import com.hlh.hlhaiagent.advisor.MyLoggerAdvisor;
import com.hlh.hlhaiagent.advisor.ProhibitedWordAdvisor;
import com.hlh.hlhaiagent.chatmemory.DatabaseChatMemory;
import com.hlh.hlhaiagent.chatmemory.FileBasedChatMemory;
import com.hlh.hlhaiagent.mapper.LoveReportMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class LoveApp {
    private final ChatClient chatClient;
    private static final String SYSTEM_PROMPT = "扮演深耕恋爱心理领域的专家。开场向用户表明身份，告知用户可倾诉恋爱难题。" +
    "围绕单身、恋爱、已婚三种状态提问：单身状态询问社交圈拓展及追求心仪对象的困扰；" +
    "恋爱状态询问沟通、习惯差异引发的矛盾；已婚状态询问家庭责任与亲属关系处理的问题。" +
    "引导用户详述事情经过、对方反应及自身想法，以便给出专属解决方案。";
    
    /**
     * 初始化 AI 客户端
     * @param dashscopeChatModel
     */
    public LoveApp(ChatModel dashscopeChatModel) {
        //1. 初始化基于文件的对话记忆
        String fielDir = System.getProperty("user.dir")+ "/tmp/chat-memory";
        FileBasedChatMemory chatMemory = new FileBasedChatMemory(fielDir);
        //2. 初始化基于内存的对话记忆
        MessageWindowChatMemory chatmemory = MessageWindowChatMemory.builder()
                                .chatMemoryRepository(new InMemoryChatMemoryRepository())
                                .maxMessages(20)  //最大记忆窗口为20条
                                .build();
        //创建 SpringAI 会话对象
        chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(  //默认拦截器   对所有请求生效
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),  //对话记忆 advisor
//                        // 自定义日志 Advisor，可按需开启
                        new MyLoggerAdvisor(),
//                        // 自定义推理增强 Advisor，可按需开启 会增加2倍输入token!
//                        new ReReadingAdvisor()
                        // (官方实现)内容安全顾问 Advisor 敏感词处理
//                        new SafeGuardAdvisor(SensitiveWords.SENSITIVE_WORDS,"你好，这个问题我暂时无法回答，让我们换个话题再聊聊吧。",0)
                        // 自己实现的敏感词顾问 Advisor
                        new ProhibitedWordAdvisor()
                ).build();
    }

    /**
     * 初始化 AI 客户端 全参构造函数
     * @param dashscopeChatModel 聊天模型
     * @param loveReportMapper 恋爱报告Mapper
     */
    @Autowired  //默认 执行这个构造函数（component只能有一个构造函数 除非用Autowired指定）
    public LoveApp(ChatModel dashscopeChatModel, LoveReportMapper loveReportMapper) {
        //3.初始化基于数据库的对话记忆
        ChatMemory chatMemory = new DatabaseChatMemory(loveReportMapper);
        chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(  //默认拦截器   对所有请求生效
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),  //对话记忆 advisor
//                        // 自定义日志 Advisor，可按需开启
                        new MyLoggerAdvisor(),
//                        // 自定义推理增强 Advisor，可按需开启 会增加2倍输入token!
//                        new ReReadingAdvisor()
                        // (官方实现)内容安全顾问 Advisor 敏感词处理
//                        new SafeGuardAdvisor(SensitiveWords.SENSITIVE_WORDS,"你好，这个问题我暂时无法回答，让我们换个话题再聊聊吧。",0)
                        // 自己实现的敏感词顾问 Advisor
                        new ProhibitedWordAdvisor()
                ).build();
    }

    /**
     * AI 基础对话（支持多轮对话记忆）
     * @param message
     * @param chatId
     * @return
     */
    public String doChat(String message,String chatId){
        ChatResponse chatResponse = this.chatClient
                .prompt()
                .user(message)  //用户提问
                .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, chatId)) // 指定会话记忆id
                .call()
                .chatResponse();
        //chatResponse 中还包含 token 等详细信息用于打印
        String content = chatResponse.getResult().getOutput().getText();

        log.info("\n ========== AI 对话记录 ==========\n" +
            "    会话ID: {}\n" +
            "    用户输入: {}\n" +
            "    AI回复: {}\n" +
            "    ================================",chatId,message,content);
        return content;
    }


    record LoveReport(String title, List<String> suggestions){

    }

    /**
     * AI 恋爱报告功能（实战结构化输出）
     * @param message
     * @param chatId
     * @return
     */
    public LoveReport doChatWithReport(String message,String chatId){
        LoveReport loveReport = this.chatClient
                .prompt()
                .system(SYSTEM_PROMPT + "每次对话后都要生成恋爱结果，标题为{用户名}的恋爱报告，内容为建议列表。")
                .user(message)  //用户提问
                .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, chatId)) // 指定会话记忆id
                .call()
                .entity(LoveReport.class);
        log.info("loveReport: {}", loveReport);
        return loveReport;
    }

}
