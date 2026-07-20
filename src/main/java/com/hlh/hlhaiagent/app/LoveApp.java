package com.hlh.hlhaiagent.app;



import com.hlh.hlhaiagent.advisor.MyLoggerAdvisor;
import com.hlh.hlhaiagent.advisor.ProhibitedWordAdvisor;
import com.hlh.hlhaiagent.chatmemory.DatabaseChatMemory;
import com.hlh.hlhaiagent.chatmemory.FileBasedChatMemory;
import com.hlh.hlhaiagent.mapper.LoveReportMapper;
import com.hlh.hlhaiagent.rag.LoveAppRagCloudAdvisorConfig;
import com.hlh.hlhaiagent.rag.LoveAppRagCustomAdvisorFactory;
import com.hlh.hlhaiagent.rag.QueryRewriter;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.List;

@Component
@Slf4j
public class LoveApp {
    private final ChatClient chatClient;
    private static final String SYSTEM_PROMPT = "扮演深耕恋爱心理领域的专家。开场向用户表明身份，告知用户可倾诉恋爱难题帮忙解答。" +
            "围绕单身、恋爱、已婚三种状态提问：单身状态询问社交圈拓展及追求心仪对象的困扰；" +
            "恋爱状态询问沟通、习惯差异引发的矛盾；已婚状态询问家庭责任与亲属关系处理的问题。" +
            "引导用户详述事情经过、对方反应及自身想法，以便给出专属解决方案。\n" +
            "【恋爱交友推荐功能专项指令】\n" +
            "当用户表达找对象、单身交友或请求恋爱对象推荐的意愿时，请按以下流程操作：\n" +
            "主动收集用户信息：至少获取 性别、年龄、教育背景、所在城市 这四项关键信息。其他字段（身高、体重、星座、职业、兴趣爱好、收入范围）可选择性询问，若用户不愿提供，允许缺失，后续匹配中忽略缺失字段。\n" +
            "调用 RAG 知识库中的 Excel 数据表，该表包含若干用户档案，字段与上述一致。\n" +
            "执行查询与筛选：\n" +
            "性别：必须为异性（与当前用户相反）。\n" +
            "所在城市：优先相同城市；若同城结果不足，可放宽至同省或邻近城市。\n" +
            "年龄：建议差值在 ±5 岁以内（可根据用户年龄灵活调整）。\n" +
            "教育背景：尽量相近（如本科配本科，硕士配硕士，或上下浮动一档）。\n" +
            "若用户提供了收入范围，可将其作为辅助筛选或排序的参考。\n" +
            "匹配度排序（优先级由高到低）：\n" +
            "① 同城（或同省/邻近）\n" +
            "② 年龄差最小\n" +
            "③ 学历/收入最接近（若收入缺失，则只比较学历）\n" +
            "按此优先级排序后，返回 前 3～5 位 最匹配的异性对象信息，逐条列出其关键字段（至少包括性别、年龄、教育背景、所在城市，若用户提供了更多信息，可一并展示）。\n" +
            "结果不足处理：若匹配人数少于 1 人，如实告知“当前库中匹配人选较少”，并建议用户放宽部分条件（如扩大城市范围或放宽年龄差）后重新查询。\n" +
            "附加建议：推荐结束后，可附一句简短的交友破冰建议（如“对方与您同城，或许可以约个咖啡聊聊”），但禁止编造库中不存在的信息。";

      // 1. 构造器注入 ChatClient
//    public LoveApp(ChatClient.Builder builder) {
//        this.chatClient = builder
//                .defaultSystem("你是恋爱顾问")
//                .build();
//    }

    // 2. 建造者模式，手动传入 ChatModel 给 ChatClient
    /**
     * 初始化 AI 客户端
     * @param dashscopeChatModel
     */
    // 不用官方的 ChatClient.Builder builder 注入 ChatClient
    // 这个 dashscopeChatModel(Spring AI Alibaba) 用于直接注入到 ChatClient 中
    // 根据名称来注册 ChatModel  用于创建 ChatClient chatClient = ChatClient.builder(chatModel).build();
    public LoveApp(ChatModel dashscopeChatModel) {
        //1. 初始化基于文件的对话记忆
        String fielDir = System.getProperty("user.dir")+ "/tmp/chat-memory";
        FileBasedChatMemory chatMemory = new FileBasedChatMemory(fielDir);
        //2. 初始化基于内存的对话记忆
        MessageWindowChatMemory inChatmemory = MessageWindowChatMemory.builder()
                                .chatMemoryRepository(new InMemoryChatMemoryRepository())
                                .maxMessages(20)  //最大记忆窗口为20条
                                .build();
        //创建 SpringAI 会话对象
        chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultSystem(SYSTEM_PROMPT)  //系统预设
                .defaultAdvisors(  //默认拦截器   对所有请求生效
                        MessageChatMemoryAdvisor.builder(inChatmemory).build(),  //对话记忆 advisor
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

//    /**
//     * 初始化 AI 客户端 全参构造函数 用数据库持久化对话记忆
//     * @param dashscopeChatModel 聊天模型
//     * @param loveReportMapper 恋爱报告Mapper
//     */
//    @Autowired  //默认执行这个构造函数（component只能有一个构造函数 除非用Autowired指定）
//    public LoveApp(ChatModel dashscopeChatModel, LoveReportMapper loveReportMapper) {
//        //3.初始化基于数据库的对话记忆
//        ChatMemory chatMemory = new DatabaseChatMemory(loveReportMapper);
//        chatClient = ChatClient.builder(dashscopeChatModel)
//                .defaultSystem(SYSTEM_PROMPT)
//                .defaultAdvisors(  //默认拦截器   对所有请求生效
//                        MessageChatMemoryAdvisor.builder(chatMemory).build(),  //对话记忆 advisor
////                        // 自定义日志 Advisor，可按需开启
//                        new MyLoggerAdvisor(),
////                        // 自定义推理增强 Advisor，可按需开启 会增加2倍输入token!
////                        new ReReadingAdvisor()
//                        // (官方实现)内容安全顾问 Advisor 敏感词处理
////                        new SafeGuardAdvisor(SensitiveWords.SENSITIVE_WORDS,"你好，这个问题我暂时无法回答，让我们换个话题再聊聊吧。",0)
//                        // 自己实现的敏感词顾问 Advisor
//                        new ProhibitedWordAdvisor()
//                ).build();
//    }

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


    record LoveReport(String title, List<String> suggestions){}

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

    // AI 恋爱知识库问答功能
    @Resource
    private VectorStore loveAppVectorStore;

    /**
     * 和 RAG 本地知识库进行对话
     * @param message
     * @param chatId
     * @return
     */
    public String doChatWithVectorStoreLocal(String message,String chatId){
        ChatResponse chatResponse = this.chatClient
                .prompt()
                .user(message)
                .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, chatId))
                // 应用 RAG 知识库问答（本地加载文档，创建简易向量数据库存储文档）
                .advisors(QuestionAnswerAdvisor.builder(loveAppVectorStore).build())
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("\n ========== AI 对话记录 ==========\n" +
                "    会话ID: {}\n" +
                "    用户输入: {}\n" +
                "    AI回复: {}\n" +
                "    ================================",chatId,message,content);
        return content;
    }

    @Resource
    private Advisor loveAppRagCloudAdvisor;

     /**
     * 和 RAG 阿里云知识库进行对话
     * @param message
     * @param chatId
     * @return
     */
    public String doChatWithRagCloud(String message,String chatId){
        ChatResponse chatResponse = this.chatClient
                .prompt()
                .user(message)
                .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, chatId))
                // 应用 RAG 知识库问答（本地加载文档，创建简易向量数据库存储文档）
//                .advisors(QuestionAnswerAdvisor.builder(loveAppVectorStore).build())
                // 应用 RAG 检索增强服务（基于云知识库服务）
                .advisors(loveAppRagCloudAdvisor)
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("\n ========== AI 对话记录 ==========\n" +
                "    会话ID: {}\n" +
                "    用户输入: {}\n" +
                "    AI回复: {}\n" +
                "    ================================",chatId,message,content);
        return content;
    }

    @Resource
    private VectorStore pgVectorVectorStore;

    /**
     * 和 RAG 阿里云 pgVector 知识库进行对话(运用 RAG 检索增强服务)
     * 手动配置向量数据库连接并存储文档
     * @param message
     * @param chatId
     * @return
     */
    public String doChatWithRagCloudEnhanced(String message,String chatId){
        ChatResponse chatResponse = this.chatClient
                .prompt()
                .user(message)
                .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, chatId))
                // 应用 RAG 知识库问答（本地加载文档，创建简易向量数据库存储文档）
//                .advisors(QuestionAnswerAdvisor.builder(loveAppVectorStore).build())
                // 应用 RAG 检索增强服务（基于云知识库服务）
//                .advisors(loveAppRagCloudAdvisor)
                // 应用 RAG 检索增强服务（基于 pgVector 向量存储）
                .advisors(QuestionAnswerAdvisor.builder(pgVectorVectorStore).build())
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("\n ========== AI 对话记录 ==========\n" +
                "    会话ID: {}\n" +
                "    用户输入: {}\n" +
                "    AI回复: {}\n" +
                "    ================================",chatId,message,content);
        return content;
    }

    @Resource
    private QueryRewriter queryRewriter;

    /**
     * 和 RAG 本地知识库进行对话
     * 增加功能：
     * 1. 查询改写：将用户提示进行改写查询，提高搜索的质量
     * 2. 增加一个自定义的 RAG 检索增强服务
     *    文档查询器：包括自定义条件过滤(比如文档元数据限制 status)，相似度阈值，返回文档数量，
     *    上下文增强：（如果没找到相关文档，则返回友好提示）
     * @param message
     * @param chatId
     * @return
     */
    public String doChatWithRewriteEnhanced(String message,String chatId){
        String rewritenMessage = queryRewriter.doQueryRewrite(message); //查询改写，利用ai改写用户提示
        ChatResponse chatResponse = this.chatClient
                .prompt()
                .user(rewritenMessage)
                .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, chatId))
                // 应用 RAG 知识库问答（本地加载文档，创建简易向量数据库存储文档）
//                .advisors(QuestionAnswerAdvisor.builder(loveAppVectorStore).build())
                // 应用 RAG 检索增强服务（基于云知识库服务）
//                .advisors(loveAppRagCloudAdvisor)
                // 应用 RAG 检索增强服务（基于 PgVector 向量存储）
//                .advisors(QuestionAnswerAdvisor.builder(pgVectorVectorStore).build())
                // 应用自定义的 RAG 检索增强服务（文档查询器 + 上下文增强）！！！
                .advisors(LoveAppRagCustomAdvisorFactory.createLoveAppRagCustomAdvisor(loveAppVectorStore, "单身"))  //限制只在知识库中搜索 status标签 = 单身 的文档
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("\n ========== AI 对话记录 ==========\n" +
                "    会话ID: {}\n" +
                "    用户输入: {}\n" +
                "    AI回复: {}\n" +
                "    ================================",chatId,message,content);
        return content;
    }

}
