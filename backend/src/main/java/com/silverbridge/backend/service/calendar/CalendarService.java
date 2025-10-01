package com.silverbridge.backend.service.calendar;

import com.silverbridge.backend.dto.calendar.CalendarDtos.*;

import java.time.LocalDate;
import java.util.List;

public interface CalendarService {

    /**
     * 특정 월의 일정 목록 조회
     */
    List<CalendarDateItem> getCalendarDates(Long userId, int year, int month);

    /**
     * 특정 날짜의 일정 조회
     */
    List<ScheduleItem> getSchedules(Long userId, LocalDate date);

    /**
     * 일정 추가
     */
    void addSchedule(Long userId, CreateScheduleRequest req);

    /**
     * 일정 수정
     */
    ScheduleItem updateSchedule(Long userId, Long scheduleId, UpdateScheduleRequest req);

    /**
     * 일정 삭제
     */
    void deleteSchedule(Long userId, Long scheduleId);
}
