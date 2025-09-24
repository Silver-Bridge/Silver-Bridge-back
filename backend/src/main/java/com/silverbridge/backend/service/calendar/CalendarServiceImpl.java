//package com.silverbridge.backend.service.calendar;
//
//import com.silverbridge.backend.dto.calendar.CalendarDtos.*;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDate;
//import java.util.List;
//
//// 필요한 Repository는 실제 네 엔티티 명에 맞춰 주입해줘.
//// 예: private final CalendarEntryRepository calendarRepository; 등
//@Service
//@RequiredArgsConstructor
//@Transactional(readOnly = true)
//public class CalendarServiceImpl implements CalendarService {
//
//    // 예시 리포지토리 (네가 이미 만들어둔 패키지/이름에 맞게 교체)
//    // private final CalendarEntryRepository calendarRepository;
//    // private final ScheduleRepository scheduleRepository;
//
//    @Override
//    public List<CalendarDateItem> getCalendarDates(Long userId, int year, int month) {
//        // TODO: DB에서 userId, year-month에 해당하는 날짜 레코드 조회
//        // return calendarRepository.findByMemberIdAndYearMonth(userId, year, month)
//        //         .stream().map(e -> new CalendarDateItem(e.getId(), e.getMemberId(), e.getDate()))
//        //         .toList();
//        throw new UnsupportedOperationException("Repository 연결 후 구현하세요.");
//    }
//
//    @Override
//    public List<ScheduleItem> getSchedules(Long userId, Long calendarId, Long scheduleId, LocalDate date) {
//        // TODO: calendarId, scheduleId, date 기준으로 스케줄 조회 (명세에 맞는 필터링)
//        // return scheduleRepository.findByCalendarAndDateAndOptionalId(...)
//        //         .stream()
//        //         .map(s -> ScheduleItem.builder()
//        //                 .id(s.getId())
//        //                 .title(s.getTitle())
//        //                 .description(s.getDescription())
//        //                 .alarmTime(s.getAlarmTime())
//        //                 .build())
//        //         .toList();
//        throw new UnsupportedOperationException("Repository 연결 후 구현하세요.");
//    }
//
//    @Override
//    @Transactional
//    public void addSchedule(Long userId, CreateScheduleRequest req) {
//        if (req.getTitle() == null || req.getTitle().isBlank() || req.getAlarmTime() == null) {
//            throw new IllegalArgumentException("필수 입력값이 누락되었습니다. (title, alarm_time)");
//        }
//        // TODO: userId의 기본/특정 캘린더를 찾아 Schedule 생성/저장
//        // scheduleRepository.save(new Schedule(...));
//        throw new UnsupportedOperationException("Repository 연결 후 구현하세요.");
//    }
//
//    @Override
//    @Transactional
//    public ScheduleItem updateSchedule(Long userId, Long calendarId, Long scheduleId, UpdateScheduleRequest req) {
//        if (req.getTitle() == null || req.getTitle().isBlank() || req.getAlarmTime() == null) {
//            throw new IllegalArgumentException("필수 입력값이 누락되었습니다. (title, alarm_time)");
//        }
//        // TODO: scheduleId로 조회 후 값 수정
//        // var s = scheduleRepository.findByIdAndCalendarIdAndUserId(...)
//        //         .orElseThrow(() -> new IllegalArgumentException("수정할 일정이 없습니다."));
//        // s.update(req.getTitle(), req.getDescription(), req.getAlarmTime());
//        // return ScheduleItem.builder()....build();
//        throw new UnsupportedOperationException("Repository 연결 후 구현하세요.");
//    }
//
//    @Override
//    @Transactional
//    public void deleteSchedule(Long userId, Long calendarId, Long scheduleId) {
//        // TODO: 존재 확인 후 삭제
//        // if (!scheduleRepository.existsByIdAndCalendarIdAndUserId(...)) throw new NotFound...
//        // scheduleRepository.deleteById(scheduleId);
//        throw new UnsupportedOperationException("Repository 연결 후 구현하세요.");
//    }
//}

package com.silverbridge.backend.service.calendar;

import com.silverbridge.backend.dto.calendar.CalendarDtos.*;
import com.silverbridge.backend.service.calendar.CalendarService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CalendarServiceImpl implements CalendarService {

    // 하드코딩 일정 저장소 (임시)
    private static final List<ScheduleItem> dummySchedules = new ArrayList<>();
    private static final List<CalendarDateItem> dummyDates = new ArrayList<>();

    static {
        // 예시 일정 하나
        dummySchedules.add(
                ScheduleItem.builder()
                        .id(1L)
                        .title("임시 일정")
                        .description("하드코딩 일정입니다.")
                        .alarmTime(OffsetDateTime.now().plusDays(1))
                        .build()
        );

        // 예시 날짜
        dummyDates.add(
                CalendarDateItem.builder()
                        .id(1L)
                        .memberId(1L)
                        .date(LocalDate.now())
                        .build()
        );
    }

    @Override
    public List<CalendarDateItem> getCalendarDates(Long userId, int year, int month) {
        System.out.println("📅 getCalendarDates 호출됨");
        return dummyDates;
    }

    @Override
    public List<ScheduleItem> getSchedules(Long userId, Long calendarId, Long scheduleId, LocalDate date) {
        System.out.println("📋 getSchedules 호출됨");
        return dummySchedules;
    }

    @Override
    public void addSchedule(Long userId, CreateScheduleRequest req) {
        System.out.println("➕ addSchedule 호출됨");

        ScheduleItem item = ScheduleItem.builder()
                .id((long) (dummySchedules.size() + 1))
                .title(req.getTitle())
                .description(req.getDescription())
                .alarmTime(req.getAlarmTime())
                .build();

        dummySchedules.add(item);
    }

    @Override
    public ScheduleItem updateSchedule(Long userId, Long calendarId, Long scheduleId, UpdateScheduleRequest req) {
        System.out.println("✏️ updateSchedule 호출됨");

        for (ScheduleItem item : dummySchedules) {
            if (item.getId().equals(scheduleId)) {
                item.setTitle(req.getTitle());
                item.setDescription(req.getDescription());
                item.setAlarmTime(req.getAlarmTime());
                return item;
            }
        }

        return null; // 없는 경우 null 반환 (실제로는 예외 던지기)
    }

    @Override
    public void deleteSchedule(Long userId, Long calendarId, Long scheduleId) {
        System.out.println("🗑️ deleteSchedule 호출됨");

        dummySchedules.removeIf(item -> item.getId().equals(scheduleId));
    }
}
