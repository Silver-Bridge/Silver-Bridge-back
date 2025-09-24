package com.silverbridge.backend.service.calendar;

import com.silverbridge.backend.dto.calendar.CalendarDtos.*;

import java.time.LocalDate;
import java.util.List;

public interface CalendarService {

    List<CalendarDateItem> getCalendarDates(Long userId, int year, int month);

    List<ScheduleItem> getSchedules(Long userId, Long calendarId, Long scheduleId, LocalDate date);

    void addSchedule(Long userId, CreateScheduleRequest req);

    ScheduleItem updateSchedule(Long userId, Long calendarId, Long scheduleId, UpdateScheduleRequest req);

    void deleteSchedule(Long userId, Long calendarId, Long scheduleId);
}
