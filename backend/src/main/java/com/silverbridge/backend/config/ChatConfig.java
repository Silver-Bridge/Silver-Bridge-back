package com.silverbridge.backend.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatConfig {

    /**
     * Spring AI가 자동 설정으로 만들어준 ChatClient.Builder를 주입받아
     * 우리가 사용할 ChatClient Bean을 생성합니다.
     */
    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder.build();
    }
}