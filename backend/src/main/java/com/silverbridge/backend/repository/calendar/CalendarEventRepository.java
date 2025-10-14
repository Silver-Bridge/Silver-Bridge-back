package com.silverbridge.backend.repository.calendar;

import com.silverbridge.backend.domain.calendar.CalendarEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

// CalendarEvent 엔티티에 대한 데이터베이스 작업을 위한 레포지토리
public interface CalendarEventRepository extends JpaRepository<CalendarEvent, Long> {
    // 특정 사용자의 특정 기간 내에 시작되는 일정 목록 조회
    List<CalendarEvent> findByUserIdAndStartAtBetween(Long userId, LocalDateTime start, LocalDateTime end);
}