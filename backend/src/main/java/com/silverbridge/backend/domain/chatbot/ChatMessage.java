package com.silverbridge.backend.domain.chatbot;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

// 챗봇 대화의 개별 메시지를 저장하는 엔티티
@Entity
@Table(name = "chat_message")
@Getter @Setter
public class ChatMessage {

    // 메시지 고유 식별자 (PK)
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 메시지가 속한 채팅 세션 (FK)
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "session_id", nullable = false)
    private ChatSession session;

    // 메시지 발신자 역할 (USER, ASSISTANT, SYSTEM)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private Role role;

    // 메시지 내용
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    // 메시지 생성 시간
    private LocalDateTime createdAt;

    // 메시지 발신자 역할을 정의하는 Enum
    public enum Role { USER, ASSISTANT, SYSTEM }

    // 엔티티 저장 전 생성 시간 자동 설정
    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}