package com.silverbridge.backend.repository.chatbot;

import com.silverbridge.backend.domain.chatbot.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findTop50BySessionIdOrderByCreatedAtDesc(Long sessionId);
}
