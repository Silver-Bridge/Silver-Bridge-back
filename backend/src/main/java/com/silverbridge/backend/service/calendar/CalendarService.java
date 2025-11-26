package com.silverbridge.backend.service.calendar;

import com.silverbridge.backend.dto.calendar.CalendarDtos.*;

import java.time.LocalDate;
import java.util.List;

// 캘린더 기능의 비즈니스 로직을 정의하는 서비스 인터페이스
public interface CalendarService {

    // 특정 월의 일정 유무 목록 조회
    List<CalendarDateItem> getCalendarDates(Long elderId, int year, int month);

    // 특정 날짜의 상세 일정 목록 조회
    List<ScheduleItem> getSchedules(Long elderId, LocalDate date);

    // 신규 일정 추가
    void addSchedule(Long elderId, CreateScheduleRequest req);

    // 기존 일정 수정
    ScheduleItem updateSchedule(Long elderId, Long scheduleId, UpdateScheduleRequest req);

    // 기존 일정 삭제
    void deleteSchedule(Long elderId, Long scheduleId);
    // 제목으로 삭제
    String deleteScheduleByTitle(Long userId, String title);

    // 일정 완료 상태 체크
    void toggleScheduleCompletion(Long elderId, Long scheduleId);
    
    // [▼ 추가] 알람 체크 메서드 정의
    List<ScheduleItem> checkAlarm(Long userId);
}