package com.silverbridge.backend.dto.chatbot;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChatVoiceResponse {
    private Long sessionId;
    private String asrText;
    private String replyText;
}
