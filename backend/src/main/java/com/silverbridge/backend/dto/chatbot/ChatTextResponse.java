package com.silverbridge.backend.dto.chatbot;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

// 텍스트 챗봇 응답을 위한 DTO
@Data
@Builder
@NoArgsConstructor // Jackson/JPA를 위한 기본 생성자
@AllArgsConstructor // Builder가 모든 필드를 인식하도록 추가
public class ChatTextResponse {
    // 현재 대화 세션 ID
    private Long sessionId;

    // 현재까지의 대화 기록
    private List<MessageDto> history;


//    // 챗봇의 텍스트 답변
//    private String replyText;
//    // [수정됨] 감정 분석 결과 필드
//    private String emotion;


}