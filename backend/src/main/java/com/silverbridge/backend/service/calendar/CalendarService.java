package com.silverbridge.backend.service.calendar;

import com.silverbridge.backend.dto.calendar.CalendarDtos.*;

import java.time.LocalDate;
import java.util.List;

// 캘린더 기능의 비즈니스 로직을 정의하는 서비스 인터페이스
public interface CalendarService {

    // 특정 월의 일정 유무 목록 조회
    List<CalendarDateItem> getCalendarDates(Long userId, int year, int month);

    // 특정 날짜의 상세 일정 목록 조회
    List<ScheduleItem> getSchedules(Long userId, LocalDate date);

    // 신규 일정 추가
    void addSchedule(Long userId, CreateScheduleRequest req);

    // 기존 일정 수정
    ScheduleItem updateSchedule(Long userId, Long scheduleId, UpdateScheduleRequest req);

    // 기존 일정 삭제
    void deleteSchedule(Long userId, Long scheduleId);
}