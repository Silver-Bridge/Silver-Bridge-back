package com.silverbridge.backend.service.calendar;

import com.silverbridge.backend.domain.User;
import com.silverbridge.backend.domain.calendar.CalendarEvent;
import com.silverbridge.backend.dto.calendar.CalendarDtos.*;
import com.silverbridge.backend.repository.UserRepository;
import com.silverbridge.backend.repository.calendar.CalendarEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import java.util.Collections;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CalendarServiceImpl implements CalendarService {

    private final CalendarEventRepository eventRepo;
    private final UserRepository userRepo;

    // 특정 월의 일정 조회
    @Override
    public List<CalendarDateItem> getCalendarDates(Long elderId, int year, int month) {

        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.plusMonths(1).minusDays(1);

        return eventRepo.findByUserIdAndStartAtBetween(
                        elderId,
                        start.atStartOfDay(),
                        end.atTime(23, 59))
                .stream()
                .map(e -> CalendarDateItem.builder()
                        .id(e.getId())
                        .memberId(e.getUser().getId())
                        .date(e.getStartAt().toLocalDate())
                        .build())
                .collect(Collectors.toList());
    }

    // 특정 날짜 일정 조회
    @Override
    public List<ScheduleItem> getSchedules(Long elderId, LocalDate date) {

        return eventRepo.findByUserIdAndStartAtBetween(
                        elderId,
                        date.atStartOfDay(),
                        date.atTime(23, 59))
                .stream()
                .map(this::toScheduleItem)
                .collect(Collectors.toList());
    }

    // 일정 생성
    @Override
    @Transactional
    public void addSchedule(Long elderId, CreateScheduleRequest req) {

        User user = userRepo.findById(elderId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // 1. 엔티티 생성 (일단 알림 시간 제외하고 빌드)
        CalendarEvent event = CalendarEvent.builder()
                .user(user)
                .title(req.getTitle())
                .description(req.getDescription())
                .startAt(req.getStartAt())
                .endAt(req.getEndAt())
                .allDay(req.getAllDay() != null ? req.getAllDay() : false)
                .location(req.getLocation())
                .repeatType(req.getRepeatType() != null ? req.getRepeatType() : CalendarEvent.RepeatType.NONE)
                .priority(req.getPriority() != null ? req.getPriority() : CalendarEvent.Priority.MEDIUM)
                .build();

        // 2. [핵심] 알림 시간 계산 및 설정
        // DTO에서 받은 '분'(예: 10)을 이용해 startAt 기준 10분 전 시간을 계산해 넣습니다.
        event.updateAlarm(req.getAlarmMinutes());

        eventRepo.save(event);
    }

    // 일정 수정
    @Override
    @Transactional
    public ScheduleItem updateSchedule(Long elderId, Long scheduleId, UpdateScheduleRequest req) {

        CalendarEvent e = eventRepo.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("수정할 일정이 없습니다."));

        if (!e.getUser().getId().equals(elderId)) {
            throw new SecurityException("본인 일정만 수정할 수 있습니다.");
        }

        // 정보 업데이트
        e.setTitle(req.getTitle());
        e.setDescription(req.getDescription());
        e.setStartAt(req.getStartAt());
        e.setEndAt(req.getEndAt());
        e.setAllDay(req.getAllDay());
        e.setLocation(req.getLocation());
        e.setRepeatType(req.getRepeatType());
        e.setPriority(req.getPriority());

        // [핵심] 수정 시에도 알림 시간 재계산 (시작 시간이 바뀌었을 수 있으므로)
        e.updateAlarm(req.getAlarmMinutes());

        return toScheduleItem(eventRepo.save(e));
    }

    // 일정 삭제
    @Override
    @Transactional
    public void deleteSchedule(Long elderId, Long scheduleId) {

        CalendarEvent e = eventRepo.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("삭제할 일정이 없습니다."));

        if (!e.getUser().getId().equals(elderId)) {
            throw new SecurityException("본인 일정만 삭제할 수 있습니다.");
        }

        eventRepo.delete(e);
    }

    // 엔티티 -> DTO 변환 편의 메서드
    private ScheduleItem toScheduleItem(CalendarEvent e) {
        return ScheduleItem.builder()
                .id(e.getId())
                .title(e.getTitle())
                .description(e.getDescription())
                // 타임존 +09:00 (KST) 적용
                .startAt(e.getStartAt())
                .endAt(e.getEndAt())
                .allDay(e.getAllDay())
                .location(e.getLocation())
                .repeatType(e.getRepeatType())
                .priority(e.getPriority())

                // [추가] 설정된 알림 분 정보도 응답에 포함
                .alarmMinutes(e.getAlarmMinutes())
                // 계산된 알림 시간이 있다면 타임존 적용해서 반환
                .alarmTime(e.getAlarmTime())
                .build();
    }

    // [▼ 추가] 프론트엔드가 1분마다 호출할 메서드
    @Override
    @Transactional
    public List<ScheduleItem> checkAlarm(Long userId) {
        // 1. 유저 정보 조회
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // [핵심] 전역 알림(alarmActive)이 꺼져있으면(false) -> 바로 빈 리스트 반환 (조회 안 함)
        if (Boolean.FALSE.equals(user.getAlarmActive())) {
            return java.util.Collections.emptyList();
        }

        // 2. 알림이 켜져 있다면 -> 시간 된 일정 조회
        LocalDateTime now = LocalDateTime.now();
        List<CalendarEvent> events = eventRepo.findByUserIdAndAlarmTimeBeforeAndIsAlarmSentFalse(userId, now);

        // 3. 조회된 알람들은 "보냄(True)" 처리 (그래야 1분 뒤에 또 안 뜸)
        for (CalendarEvent event : events) {
            event.markAlarmAsSent();
        }

        // 4. 결과 반환
        return events.stream()
                .map(this::toScheduleItem)
                .collect(Collectors.toList());
    }
}