package com.silverbridge.backend.dto.chatbot;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChatTextRequest {
    @NotBlank
    private String text;
    private String regionCode;  // "gs"|"jl"|"std"
    private Long sessionId;
}
