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

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CalendarServiceImpl implements CalendarService {

    private final CalendarEventRepository eventRepo;
    private final UserRepository userRepo;

    // 1. 월별 일정 조회
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

    // 2. 특정 날짜 상세 일정 조회
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

    // 3. 일정 추가
    @Override
    @Transactional
    public void addSchedule(Long elderId, CreateScheduleRequest req) {
        User user = userRepo.findById(elderId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

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
                .isCompleted(false) // 기본값 false
                .build();

        event.updateAlarm(req.getAlarmMinutes());
        eventRepo.save(event);
    }

    // 4. 일정 수정
    @Override
    @Transactional
    public ScheduleItem updateSchedule(Long elderId, Long scheduleId, UpdateScheduleRequest req) {
        CalendarEvent e = eventRepo.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("수정할 일정이 없습니다."));

        if (!e.getUser().getId().equals(elderId)) {
            throw new SecurityException("본인 일정만 수정할 수 있습니다.");
        }

        e.setTitle(req.getTitle());
        e.setDescription(req.getDescription());
        e.setStartAt(req.getStartAt());
        e.setEndAt(req.getEndAt());
        e.setAllDay(req.getAllDay());
        e.setLocation(req.getLocation());
        e.setRepeatType(req.getRepeatType());
        e.setPriority(req.getPriority());

        // [추가된 부분]
        if (req.getIsCompleted() != null) {
            e.setIsCompleted(req.getIsCompleted());
        }

        e.updateAlarm(req.getAlarmMinutes());
        return toScheduleItem(eventRepo.save(e));
    }

    // 5. 일정 삭제
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

    // 챗봇으로 일정 삭제
    @Override
    @Transactional
    public String deleteScheduleByTitle(Long userId, String title) {
        // 1. 현재 시간 이후의 일정 중, 제목이 포함된 가장 빠른 일정을 찾음
        CalendarEvent event = eventRepo.findFirstByUserIdAndTitleContainingAndStartAtAfterOrderByStartAtAsc(
                userId, title, LocalDateTime.now()
        ).orElse(null);

        // 2. 없으면 실패 메시지
        if (event == null) {
            return "삭제할 일정을 못 찾았습니더. (" + title + ")";
        }

        // 3. 찾았으면 삭제
        eventRepo.delete(event);
        return "일정을 삭제했습니더. (" + event.getTitle() + ", " + event.getStartAt().toLocalDate() + ")";
    }

    // 6. [신규 기능] 일정 완료 토글
    @Override
    @Transactional
    public void toggleScheduleCompletion(Long elderId, Long scheduleId) {
        CalendarEvent e = eventRepo.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("일정을 찾을 수 없습니다."));

        if (!e.getUser().getId().equals(elderId)) {
            throw new SecurityException("본인 일정만 변경할 수 있습니다.");
        }

        e.toggleCompletion();
    }

    // 7. 알람 체크 (1분마다 호출됨)
    @Override
    @Transactional
    public List<ScheduleItem> checkAlarm(Long userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        if (Boolean.FALSE.equals(user.getAlarmActive())) {
            return java.util.Collections.emptyList();
        }

        LocalDateTime now = LocalDateTime.now();
        List<CalendarEvent> events = eventRepo.findByUserIdAndAlarmTimeBeforeAndIsAlarmSentFalse(userId, now);

        for (CalendarEvent event : events) {
            event.markAlarmAsSent();
        }

        return events.stream()
                .map(this::toScheduleItem)
                .collect(Collectors.toList());
    }

    // 내부 편의 메서드: Entity -> DTO 변환
    private ScheduleItem toScheduleItem(CalendarEvent e) {
        return ScheduleItem.builder()
                .id(e.getId())
                .title(e.getTitle())
                .description(e.getDescription())
                .startAt(e.getStartAt())
                .endAt(e.getEndAt())
                .allDay(e.getAllDay())
                .location(e.getLocation())
                .repeatType(e.getRepeatType())
                .priority(e.getPriority())
                .alarmMinutes(e.getAlarmMinutes())
                .alarmTime(e.getAlarmTime())
                .isCompleted(e.getIsCompleted())
                .build();
    }
}