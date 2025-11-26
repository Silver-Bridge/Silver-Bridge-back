package com.silverbridge.backend.dto.chatbot;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

// 음성 챗봇 응답을 위한 DTO
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatVoiceResponse {

    // 현재 대화를 진행 중인 사용자 ID
    private Long userId;

    // 현재 대화 세션 ID
    private Long sessionId;

    // 채팅 제목
    private String title;

    // [수정] 현재까지의 대화 기록
    private List<MessageDto> history;

    // [수정] 챗봇의 "음성" 응답 URL
    private String replyAudioUrl;

    //    // 사용자 음성을 텍스트로 변환한 결과 (ASR)
    //    private String asrText;
    //    // 챗봇의 텍스트 답변
    //    private String replyText;
    //
    //    // 감정 분석 결과 필드 (원본 코드에 이미 반영됨)
    //    private String emotion;
}