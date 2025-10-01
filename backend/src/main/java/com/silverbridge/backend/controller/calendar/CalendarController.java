package com.silverbridge.backend.controller.calendar;

import com.silverbridge.backend.dto.calendar.CalendarDtos.*;
import com.silverbridge.backend.service.calendar.CalendarService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/calendar")
public class CalendarController {

    private final CalendarService calendarService;

    /**
     * 특정 월의 일정 목록 조회
     * GET /api/calendar/{userId}?year=2025&month=9
     */
    @GetMapping("/{userId}")
    public ResponseEntity<?> getCalendar(
            @PathVariable Long userId,
            @RequestParam int year,
            @RequestParam int month
    ) {
        try {
            List<CalendarDateItem> items = calendarService.getCalendarDates(userId, year, month);
            return ResponseEntity.ok(CalendarDateListResponse.builder().body(items).build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(SimpleMessageResponse.builder()
                    .code(613)
                    .message("날짜 형식이 올바르지 않습니다.")
                    .build());
        }
    }

    /**
     * 특정 날짜의 일정 조회
     * GET /api/calendar/{userId}/schedules?date=2025-09-28
     */
    @GetMapping("/{userId}/schedules")
    public ResponseEntity<?> getSchedules(
            @PathVariable Long userId,
            @RequestParam String date
    ) {
        try {
            LocalDate d = LocalDate.parse(date); // ISO-8601 yyyy-MM-dd
            List<ScheduleItem> items = calendarService.getSchedules(userId, d);
            return ResponseEntity.ok(ScheduleListResponse.builder().body(items).build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(SimpleMessageResponse.builder()
                    .code(614)
                    .message("일정을 조회할 수 없습니다.")
                    .build());
        }
    }

    /**
     * 일정 추가
     * POST /api/calendar/{userId}/add
     */
    @PostMapping("/{userId}/add")
    public ResponseEntity<?> addSchedule(
            @PathVariable Long userId,
            @RequestBody CreateScheduleRequest req
    ) {
        try {
            calendarService.addSchedule(userId, req);
            return ResponseEntity.ok(SimpleMessageResponse.builder()
                    .code(200)
                    .message("일정 추가에 성공하였습니다.")
                    .build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(SimpleMessageResponse.builder()
                    .code(801)
                    .message(e.getMessage())
                    .build());
        }
    }

    /**
     * 일정 수정
     * PUT /api/calendar/{userId}/schedule/{scheduleId}
     */
    @PutMapping("/{userId}/schedule/{scheduleId}")
    public ResponseEntity<?> updateSchedule(
            @PathVariable Long userId,
            @PathVariable Long scheduleId,
            @RequestBody UpdateScheduleRequest req
    ) {
        try {
            ScheduleItem updated = calendarService.updateSchedule(userId, scheduleId, req);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(SimpleMessageResponse.builder()
                    .code(801)
                    .message(e.getMessage())
                    .build());
        }
    }

    /**
     * 일정 삭제
     * DELETE /api/calendar/{userId}/schedule/{scheduleId}
     */
    @DeleteMapping("/{userId}/schedule/{scheduleId}")
    public ResponseEntity<?> deleteSchedule(
            @PathVariable Long userId,
            @PathVariable Long scheduleId
    ) {
        try {
            calendarService.deleteSchedule(userId, scheduleId);
            return ResponseEntity.ok(SimpleMessageResponse.builder()
                    .code(200)
                    .message("일정 삭제에 성공하였습니다.")
                    .build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(SimpleMessageResponse.builder()
                    .code(802)
                    .message("삭제할 일정이 없습니다.")
                    .build());
        }
    }
}
