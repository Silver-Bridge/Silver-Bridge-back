package com.silverbridge.backend.dto.chatbot;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor // (role, content, emotion) 3개 필드 생성자
public class MessageDto {

    // 메시지 역할 (system, user, assistant)
    private String role;

    // 메시지 내용
    private String content;

    // 사용자의 감정 (user 메시지에만 사용, assistant는 null)
    private String emotion;

    public MessageDto(String role, String content) {
        this.role = role;
        this.content = content;
        this.emotion = null; // 기본값 null
    }
}