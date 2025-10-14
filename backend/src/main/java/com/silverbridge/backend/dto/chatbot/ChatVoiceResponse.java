package com.silverbridge.backend.dto.chatbot;

import lombok.Builder;
import lombok.Data;

// 음성 챗봇 응답을 위한 DTO
@Data
@Builder
public class ChatVoiceResponse {
    // 현재 대화 세션 ID
    private Long sessionId;
    // 사용자 음성을 텍스트로 변환한 결과 (ASR)
    private String asrText;
    // 챗봇의 텍스트 답변
    private String replyText;
}