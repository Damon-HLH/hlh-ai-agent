package com.hlh.hlhaiagent.controller;


import com.hlh.hlhaiagent.agent.HlhManus;
import com.hlh.hlhaiagent.app.LoveApp;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;

@RestController
@RequestMapping("/ai")
public class AiController {

    @Resource
    private LoveApp loveApp;

    @Resource
    private ToolCallback[] allTools;

    @Resource
    private ChatModel dashscopeChatModel;

    // Spirngboot 单例 自动注入，我们每次对话需要启动一个新的 HlhManus 实例
    // 不然是同时调用同一个manus 会阻塞
//    @Resource
//    private HlhManus hlhManus;

    /**
     * 同步调用 AI 恋爱大师应用
     *
     * @param message
     * @param chatId
     * @return
     */
    @GetMapping("/love_app/chat/sync")
    public String doChatWithLoveAppSync(String message, String chatId) {
        return loveApp.doChat(message, chatId);
    }

    /**
     * SSE 流式调用 AI 恋爱大师应用-以下有三种方式都可以实现
     * 1.直接返回 Flux<String> + SSE MediaType
     * 2.封装为 ServerSentEvent 对象
     * 3.手动控制 SseEmitter:适用于需要精细控制流的场景（如超时处理）：
     *
     * @param message
     * @param chatId
     * @return
     */
    @GetMapping(value = "/love_app/chat/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE) //文本流式返回
    public Flux<String> doChatWithLoveAppSSE(String message, String chatId) {
        return loveApp.doChatByStream(message, chatId);
    }

    @GetMapping(value = "/love_app/chat/server_sent_event")
    public Flux<ServerSentEvent<String>> doChatWithLoveAppServerSentEvent(String message, String chatId) {
        return loveApp.doChatByStream(message, chatId)
                .map(chunk -> ServerSentEvent.<String>builder()
                        .data(chunk)
                        .build());  //标准化实践格式
    }

    @GetMapping(value = "/love_app/chat/sse_emmitter")
    public SseEmitter doChatWithLoveAppSseEmitter(String message, String chatId) {
        // 创建一个超时时间较长的 SseEmitter
        SseEmitter sseEmitter = new SseEmitter(180000L); //3分钟超时
        // 获取 Flux 响应式数据流并直接通过订阅推送给 SseEmitter
        loveApp.doChatByStream(message, chatId)
                .subscribe(
                        chunk -> {
                            try {
                                sseEmitter.send(chunk); //发送数据块
                            } catch (IOException e) {
//                        throw new RuntimeException(e);
                                sseEmitter.completeWithError(e);
                            }
                        }, sseEmitter::completeWithError, //  错误处理
                        sseEmitter::complete      //  流结束
                );
        return sseEmitter;
    }

    /**
     * 流式调用 Manus 超级智能体
     *
     * @param message
     * @return
     */
    @GetMapping("/manus/chat")
    public SseEmitter doChatWithManus(String message) {
        HlhManus hlhManus = new HlhManus(allTools, dashscopeChatModel);
        return hlhManus.runStream(message);
    }


}
