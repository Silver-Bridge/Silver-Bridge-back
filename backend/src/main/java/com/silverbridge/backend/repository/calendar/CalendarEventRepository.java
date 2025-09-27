package com.silverbridge.backend.repository.calendar;

import com.silverbridge.backend.domain.calendar.CalendarEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;


public interface CalendarEventRepository extends JpaRepository<CalendarEvent, Long> {
    List<CalendarEvent> findByUserIdAndStartAtBetween(Long userId, LocalDateTime from, LocalDateTime to);
}
