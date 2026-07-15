package com.hlh.hlhaiagent.advisor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClientMessageAggregator;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import reactor.core.publisher.Flux;

public class MyLoggerAdvisor implements CallAdvisor, StreamAdvisor {

	private static final Logger logger = LoggerFactory.getLogger(SimpleLoggerAdvisor.class);

	@Override
	public String getName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public int getOrder() {
		return 0;
	}


	@Override
	public ChatClientResponse adviseCall(ChatClientRequest chatClientRequest, CallAdvisorChain callAdvisorChain) {
		logRequest(chatClientRequest);

		ChatClientResponse chatClientResponse = callAdvisorChain.nextCall(chatClientRequest);

		logResponse(chatClientResponse);

		return chatClientResponse;
	}

	@Override
	public Flux<ChatClientResponse> adviseStream(ChatClientRequest chatClientRequest,
                                                 StreamAdvisorChain streamAdvisorChain) {
		logRequest(chatClientRequest);

		Flux<ChatClientResponse> chatClientResponses = streamAdvisorChain.nextStream(chatClientRequest);

		return new ChatClientMessageAggregator().aggregateChatClientResponse(chatClientResponses, this::logResponse);
	}

	private void logRequest(ChatClientRequest request) {
		logger.info("AI request: {}", request.prompt().getUserMessage().getText());
	}

	private void logResponse(ChatClientResponse chatClientResponse) {
		logger.info("AI response: {}", chatClientResponse.chatResponse().getResult().getOutput().getText());
	}

}