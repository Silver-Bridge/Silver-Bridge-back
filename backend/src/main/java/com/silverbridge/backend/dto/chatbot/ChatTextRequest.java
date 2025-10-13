package com.silverbridge.backend.dto.chatbot;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

// 텍스트 챗봇 요청을 위한 DTO
@Data
public class ChatTextRequest {
    // 사용자가 입력한 텍스트 메시지
    @NotBlank
    private String text;
    // 챗봇 응답에 사용할 지역 방언 코드 ("gs"|"jl"|"std")
    private String regionCode;
    // 기존 대화 세션 ID (null인 경우 새 세션 생성)
    private Long sessionId;
}