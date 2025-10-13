package com.silverbridge.backend.domain.chatbot;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

// 챗봇 대화 세션을 저장하는 엔티티
@Entity
@Table(name = "chat_session")
@Getter @Setter
public class ChatSession {

    // 세션 고유 식별자 (PK)
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 세션을 생성한 사용자 ID
    private Long userId;

    // 챗봇 응답에 사용할 지역 방언 코드 ("gs", "jl", "std")
    private String regionCode;

    // 세션 생성 시간
    private LocalDateTime createdAt;
    // 세션 마지막 업데이트 시간
    private LocalDateTime updatedAt;

    // 엔티티 저장 전 초기값 설정 (생성/업데이트 시간, 기본 지역 코드)
    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
        if (this.regionCode == null) this.regionCode = "std";
    }

    // 엔티티 업데이트 전 수정 시간 갱신
    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}