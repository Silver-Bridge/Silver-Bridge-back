package com.silverbridge.backend.dto.chatbot;

import lombok.AllArgsConstructor;
import lombok.Data;

// 대화 기록 등에서 단일 메시지를 표현하기 위한 DTO
@Data
@AllArgsConstructor
public class MessageDto {
    // 메시지 발신자 역할 ("user"|"assistant"|"system")
    private String role;
    // 메시지 내용
    private String content;
}