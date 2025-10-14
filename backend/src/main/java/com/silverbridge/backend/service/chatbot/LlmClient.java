package com.silverbridge.backend.service.chatbot;

import com.silverbridge.backend.dto.chatbot.MessageDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

// 거대 언어 모델(LLM) 서버 API 호출을 위한 클라이언트
@Component
public class LlmClient {

    // LLM 챗봇 API 엔드포인트
    @Value("${chatbot.llm.endpoint:http://localhost:9100/llm/chat}")
    private String llmEndpoint;

    // 메시지 목록(프롬프트)을 LLM 서버로 보내 답변 생성
    public String chat(List<MessageDto> messages, boolean seniorFriendly) {
        // TODO: 실제 LLM API 연동 구현 필요
        return "노인 친화 답변(스텁)";
    }
}