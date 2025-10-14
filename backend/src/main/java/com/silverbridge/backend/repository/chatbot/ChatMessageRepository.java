package com.silverbridge.backend.repository.chatbot;

import com.silverbridge.backend.domain.chatbot.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// ChatMessage 엔티티에 대한 데이터베이스 작업을 위한 레포지토리
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    // 특정 세션 ID에 해당하는 최근 메시지 50개 조회
    List<ChatMessage> findTop50BySessionIdOrderByCreatedAtDesc(Long sessionId);
}