package com.hlh.hlhaiagent.advisor;

import cn.hutool.core.io.resource.ClassPathResource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import org.springframework.ai.chat.model.Generation;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 违禁词校验 Advisor
 * 检查用户输入是否包含违禁词 从本地加载违禁词
 * 违禁词来源 https://gitee.com/crazypoo/badwords
 */
@Slf4j
public class ProhibitedWordAdvisor implements CallAdvisor, StreamAdvisor {

    private static final String PROHIBITED_WORDS_FILE = "prohibited/prohibited-words.txt" ;
    private final List<String> prohibitedWords;
    private static final String PROHIBITED_WORD_RESPONSE = "您输入的信息包含敏感内容，请重新输入。";

    public ProhibitedWordAdvisor() {
        //加载违禁词
        this.prohibitedWords = loadProhibitedWordsFromFile(PROHIBITED_WORDS_FILE);
        log.info("初始化违禁词Advisor，违禁词数量: {}", prohibitedWords.size());
    }

    public ProhibitedWordAdvisor(List<String> prohibitedWords) {
        this.prohibitedWords = prohibitedWords;
    }

    /**
     * 创建违禁词Advisor，从指定文件读取违禁词列表
     */
    public ProhibitedWordAdvisor(String prohibitedWordsFile) {
        this.prohibitedWords = loadProhibitedWordsFromFile(prohibitedWordsFile);
        log.info("初始化违禁词Advisor，违禁词数量: {}", prohibitedWords.size());
    }

    /**
     * 从文件加载违禁词列表
     */
    private List<String> loadProhibitedWordsFromFile(String filePath) {
        try {
            //：ClassPathResource 从 classpath 根目录开始查找，即 src/main/resources/
            var resource = new ClassPathResource(filePath);
            var reader = new BufferedReader(
                    new InputStreamReader(resource.getStream(), StandardCharsets.UTF_8));

            List<String> words = reader.lines()
                    .filter(StringUtils::hasText)
                    .map(String::trim)
                    .collect(Collectors.toList());

            log.info("从文件 {} 加载违禁词 {} 个", filePath, words.size());
            return words;
        } catch (Exception e) {
            log.error("加载违禁词文件 {} 失败", filePath, e);
            return new ArrayList<>();
        }
    }

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest chatClientRequest, CallAdvisorChain callAdvisorChain) {
        String userText = chatClientRequest.prompt().getUserMessage().getText();
        if (containsProhibitedWord(userText)) {
            log.warn("检测到用户输入包含违禁词");
            return buildProhibitedResponse();
        }

        return callAdvisorChain.nextCall(chatClientRequest);
    }

    @Override
    public Flux<ChatClientResponse> adviseStream(ChatClientRequest chatClientRequest, StreamAdvisorChain streamAdvisorChain) {
        String userText = chatClientRequest.prompt().getUserMessage().getText();

        if (containsProhibitedWord(userText)) {
            log.warn("检测到用户输入包含违禁词(Stream)");
            return Flux.just(buildProhibitedResponse());
        }

        return streamAdvisorChain.nextStream(chatClientRequest);
    }

    private ChatClientResponse buildProhibitedResponse() {
        AssistantMessage assistantMessage = new AssistantMessage(PROHIBITED_WORD_RESPONSE);
        Generation generation = new Generation(assistantMessage);
        return ChatClientResponse.builder()
                .chatResponse(ChatResponse.builder().generations(List.of(generation)).build())
                .build();
    }

    /**
     * 检查文本中是否包含违禁词
     */
    private boolean containsProhibitedWord(String text) {
        if (!StringUtils.hasText(text)) {
            return false;
        }

        for (String word : prohibitedWords) {
            if (text.toLowerCase().contains(word.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public int getOrder() {
        return -100; // 确保在其他Advisor之前执行
    }

}
