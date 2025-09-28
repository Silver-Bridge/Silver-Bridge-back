package com.silverbridge.backend.dto.chatbot;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MessageDto {
    private String role;     // "user"|"assistant"|"system"
    private String content;
}
