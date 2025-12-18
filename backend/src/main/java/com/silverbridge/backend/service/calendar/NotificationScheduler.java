package com.silverbridge.backend.service.calendar;

import com.silverbridge.backend.domain.calendar.CalendarEvent;
import com.silverbridge.backend.repository.calendar.CalendarEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
// import org.springframework.scheduling.annotation.Scheduled; // [주석 처리]
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
// @Service // 이 줄을 주석 처리해서 스프링이 이 파일을 무시하게 만듭니다.
@RequiredArgsConstructor
public class NotificationScheduler {

    private final CalendarEventRepository calendarEventRepository;

    @Transactional
    public void checkAndSendAlarms() {
        LocalDateTime now = LocalDateTime.now();

        // 알람 대상 조회
        List<CalendarEvent> eventsToNotify = calendarEventRepository
                .findAllByAlarmTimeBeforeAndIsAlarmSentFalse(now);

        if (!eventsToNotify.isEmpty()) {
            log.info("🔔 [알림 체크] 발송할 일정 {}개를 발견했습니다.", eventsToNotify.size());
        }

        for (CalendarEvent event : eventsToNotify) {
            try {
                sendPushNotification(event);
                event.markAlarmAsSent();

            } catch (Exception e) {
                log.error("⚠️ 알림 발송 실패 (Event ID: {}): {}", event.getId(), e.getMessage());
            }
        }
    }

    private void sendPushNotification(CalendarEvent event) {
        System.out.println("========================================");
        System.out.println("[푸시 알림 발송] 🔔 띠링!");
        System.out.println(" - 일정: " + event.getTitle());
        System.out.println(" - 시간: " + event.getStartAt());
        System.out.println("========================================");
    }
}