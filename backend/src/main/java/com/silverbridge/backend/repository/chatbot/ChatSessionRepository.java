package com.silverbridge.backend.repository.chatbot;

import com.silverbridge.backend.domain.chatbot.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {
    Optional<ChatSession> findFirstByUserIdOrderByUpdatedAtDesc(Long userId);
    List<ChatSession> findByUserIdOrderByCreatedAtDesc(Long userId);
}
