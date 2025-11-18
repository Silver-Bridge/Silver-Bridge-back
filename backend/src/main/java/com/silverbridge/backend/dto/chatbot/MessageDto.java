package com.silverbridge.backend.dto.chatbot;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 챗봇 대화 메시지 DTO (History 배열의 요소)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor // (role, content, emotion) 3개 필드 생성자
public class MessageDto {

    // 메시지 역할 (system, user, assistant)
    private String role;

    // 메시지 내용
    private String content;

    // [수정] 사용자의 감정 (user 메시지에만 사용, assistant는 null)
    private String emotion;

    /**
     * [수정] ChatService의 latestHistory 메서드와 호환성을 위한 2-args 생성자
     * (DB에서 읽어올 때는 emotion이 null)
     */
    public MessageDto(String role, String content) {
        this.role = role;
        this.content = content;
        this.emotion = null; // 기본값 null
    }
}