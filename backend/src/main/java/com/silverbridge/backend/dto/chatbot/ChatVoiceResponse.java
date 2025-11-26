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

    // [추가됨] 채팅방 제목 (이 필드가 없어서 에러가 났었습니다)
    private String title;

    // 현재까지의 대화 기록
    private List<MessageDto> history;

    // 챗봇의 "음성" 응답 URL
    private String replyAudioUrl;
}