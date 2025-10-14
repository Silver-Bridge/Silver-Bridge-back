package com.silverbridge.backend.domain.calendar;

import com.silverbridge.backend.domain.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

// 캘린더의 개별 일정을 저장하는 엔티티
@Entity
@Table(name = "calendar_event")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CalendarEvent {

    // 일정 고유 식별자 (PK)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 일정을 소유한 사용자 (FK)
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    // 일정 제목
    @Column(nullable = false)
    private String title;

    // 일정 상세 설명
    @Column(columnDefinition = "TEXT")
    private String description;

    // 일정 시작 시간
    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;

    // 일정 종료 시간
    @Column(name = "end_at", nullable = false)
    private LocalDateTime endAt;

    // 하루 종일 지속되는 일정 여부
    @Column(name = "all_day")
    @Builder.Default
    private Boolean allDay = false;

    // 일정 장소
    private String location;

    // 반복 규칙 (NONE, DAILY, WEEKLY, MONTHLY)
    @Enumerated(EnumType.STRING)
    @Column(name = "repeat_type", nullable = false)
    @Builder.Default
    private RepeatType repeatType = RepeatType.NONE;

    // 중요도 (LOW, MEDIUM, HIGH)
    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false)
    @Builder.Default
    private Priority priority = Priority.MEDIUM;

    // 알림 시각 (일정 시작 기준)
    @Column(name = "alarm_time")
    private LocalDateTime alarmTime;

    // 일정 생성 시간
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // 일정 최종 수정 시간
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 엔티티 저장 전 생성/수정 시간 초기화
    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    // 엔티티 업데이트 전 수정 시간 초기화
    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // 반복 규칙 정의
    public enum RepeatType {
        NONE, DAILY, WEEKLY, MONTHLY
    }

    // 중요도 정의
    public enum Priority {
        LOW, MEDIUM, HIGH
    }
}