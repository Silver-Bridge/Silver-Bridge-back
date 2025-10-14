package com.silverbridge.backend.repository.chatbot;

import com.silverbridge.backend.domain.chatbot.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

// ChatSession 엔티티에 대한 데이터베이스 작업을 위한 레포지토리
public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {
    // 특정 사용자의 가장 최근에 업데이트된 세션 조회
    Optional<ChatSession> findFirstByUserIdOrderByUpdatedAtDesc(Long userId);
    // 특정 사용자의 모든 세션을 생성 시간순으로 조회
    List<ChatSession> findByUserIdOrderByCreatedAtDesc(Long userId);
}