package com.hlh.hlhaiagent.app;

import cn.hutool.core.lang.UUID;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
class LoveAppTest {

    @Resource
    private LoveApp loveApp;

    @Test
    void doChat() {
        String chatId = UUID.randomUUID().toString();
        //第一轮
        String message = "你好，我是hlh";
        String answer = loveApp.doChat(message, chatId);
        //第二轮
        message = "我想让孙东篱更爱我，该怎么做？";
        answer = loveApp.doChat(message, chatId);
        Assertions.assertNotNull(answer);
        //第三轮
        message = "我的另一半叫什么来着？刚跟你说过，请你帮我回忆一下";
        answer = loveApp.doChat(message, chatId);
        Assertions.assertNotNull(answer);
    }


    @Test
    void doChatWithSensitiveWords() {
        String chatId = UUID.randomUUID().toString();
        String message = "你好，我是爱爱";
        String answer = loveApp.doChat(message, chatId);
    }


    @Test
    void doChatWithReport() {
        String chatId = UUID.randomUUID().toString();
        //第一轮
        String message = "你好，我是hlh;我想让另一半更爱我，该怎么做？";
        LoveApp.LoveReport loveReport = loveApp.doChatWithReport(message, chatId);
        Assertions.assertNotNull(loveReport);
    }

    @Test
    void doChatWithDatabase() {
        String chatId = UUID.randomUUID().toString();
        String message = "你好，我是hlh;你能告诉我如何让孙东篱更爱我吗？";
        LoveApp.LoveReport loveReport = loveApp.doChatWithReport(message, chatId);
        Assertions.assertNotNull(loveReport);
    }

}