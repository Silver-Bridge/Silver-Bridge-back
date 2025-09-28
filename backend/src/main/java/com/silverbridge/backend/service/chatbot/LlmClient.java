package com.silverbridge.backend.service.chatbot;

import com.silverbridge.backend.dto.chatbot.MessageDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LlmClient {

    @Value("${chatbot.llm.endpoint:http://localhost:9100/llm/chat}")
    private String llmEndpoint;

    public String chat(List<MessageDto> messages, boolean seniorFriendly) {
        // 지금은 더미 응답만 반환
        return "노인 친화 답변(스텁)";
    }
}
