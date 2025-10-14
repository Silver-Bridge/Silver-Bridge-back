package com.silverbridge.backend.dto.chatbot;

import lombok.Builder;
import lombok.Data;

import java.util.List;

// 텍스트 챗봇 응답을 위한 DTO
@Data
@Builder
public class ChatTextResponse {
    // 현재 대화 세션 ID
    private Long sessionId;
    // 챗봇의 텍스트 답변
    private String replyText;
    // 현재까지의 대화 기록
    private List<MessageDto> history;
}