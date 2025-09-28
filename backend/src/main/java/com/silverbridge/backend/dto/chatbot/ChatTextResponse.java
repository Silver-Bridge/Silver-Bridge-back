package com.silverbridge.backend.dto.chatbot;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ChatTextResponse {
    private Long sessionId;
    private String replyText;
    private List<MessageDto> history;
}
