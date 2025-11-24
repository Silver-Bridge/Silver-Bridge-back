package com.silverbridge.backend.service.calendar;

import com.silverbridge.backend.domain.calendar.CalendarEvent;
import com.silverbridge.backend.repository.calendar.CalendarEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationScheduler {

    private final CalendarEventRepository calendarEventRepository;

    // (나중에 필요하면 추가)
    // private final FcmService fcmService;
    // private final SmsService smsService;

    /**
     * 1분마다 실행되어 알림을 보내야 할 일정이 있는지 확인합니다.
     * fixedRate = 60000 (60초)
     */
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void checkAndSendAlarms() {
        // 현재 시간 (초 단위 절삭을 위해 withSecond(0).withNano(0)를 쓸 수도 있지만,
        // Before 조건이므로 현재 시점 포함 과거의 미발송 알림을 모두 찾습니다.)
        LocalDateTime now = LocalDateTime.now();

        // 1. "알림 설정 시간이 지났고(과거)", "아직 발송되지 않은(false)" 일정 조회
        List<CalendarEvent> eventsToNotify = calendarEventRepository
                .findAllByAlarmTimeBeforeAndIsAlarmSentFalse(now);

        if (!eventsToNotify.isEmpty()) {
            log.info("🔔 [알림 체크] 발송할 일정 {}개를 발견했습니다.", eventsToNotify.size());
        }

        for (CalendarEvent event : eventsToNotify) {
            try {
                // 2. 알림 발송 로직 실행
                sendPushNotification(event);

                // 3. 발송 완료 처리 (중복 발송 방지)
                // (@Transactional 덕분에 save 호출 없이도 변경 감지로 DB 업데이트됨)
                event.setIsAlarmSent(true);

            } catch (Exception e) {
                log.error("⚠️ 알림 발송 실패 (Event ID: {}): {}", event.getId(), e.getMessage());
                // 실패 시 isAlarmSent를 true로 바꾸지 않아 다음 턴에 재시도하게 할 수도 있고,
                // 에러 루프를 방지하기 위해 별도 처리할 수도 있음. 여기선 일단 넘어갑니다.
            }
        }
    }

    // 실제 알림 발송 메서드
    private void sendPushNotification(CalendarEvent event) {
        // 현재는 콘솔 로그로 대체 (나중에 FCM/SMS 연동)
        System.out.println("========================================");
        System.out.println("[푸시 알림 발송] 🔔 띠링!");
        System.out.println(" - 일정: " + event.getTitle());
        System.out.println(" - 시간: " + event.getStartAt()); // startDateTime -> startAt으로 수정 (Entity 필드명 일치)
        System.out.println(" - 내용: " + (event.getDescription() != null ? event.getDescription() : "내용 없음"));
        System.out.println("========================================");
    }
}