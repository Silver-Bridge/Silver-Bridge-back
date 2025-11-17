package com.silverbridge.backend.dto.chatbot;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// 음성 챗봇 응답을 위한 DTO
@Data
@Builder
@NoArgsConstructor // Jackson/JPA를 위한 기본 생성자
@AllArgsConstructor // Builder가 모든 필드를 인식하도록 추가
public class ChatVoiceResponse {
    // 현재 대화 세션 ID
    private Long sessionId;
    // 사용자 음성을 텍스트로 변환한 결과 (ASR)
    private String asrText;
    // 챗봇의 텍스트 답변
    private String replyText;

    // [수정됨] 감정 분석 결과 필드
    private String emotion;
}