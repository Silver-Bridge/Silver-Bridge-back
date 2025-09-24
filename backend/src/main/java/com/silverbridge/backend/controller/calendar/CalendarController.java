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

    // 캘린더 목록: GET /api/calender/{userId}?year=2024&month=8
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
                    .code(613) // 명세의 예시 코드
                    .message("날짜 형식에 맞지않습니다.")
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(SimpleMessageResponse.builder()
                    .code(401)
                    .message("요청 권한이 없습니다.")
                    .build());
        }
    }

    // 일정 상세보기: GET /api/calender/{userId}/{calendarId}/schedule/{scheduleId}?date=2024-08-15
    @GetMapping("/{userId}/{calendarId}/schedule/{scheduleId}")
    public ResponseEntity<?> getScheduleDetail(
            @PathVariable Long userId,
            @PathVariable Long calendarId,
            @PathVariable Long scheduleId,
            @RequestParam String date
    ) {
        try {
            LocalDate d = LocalDate.parse(date); // ISO-8601 yyyy-MM-dd
            List<ScheduleItem> items = calendarService.getSchedules(userId, calendarId, scheduleId, d);
            return ResponseEntity.ok(ScheduleListResponse.builder().body(items).build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(SimpleMessageResponse.builder()
                    .code(401)
                    .message("요청 권한이 없습니다.")
                    .build());
        }
    }

    // 일정 추가: POST /api/calender/{userId}/add
    @PostMapping("/{userId}/add")
    public ResponseEntity<?> addSchedule(
            @PathVariable Long userId,
            @RequestBody CreateScheduleRequest req
    ) {
        try {
            calendarService.addSchedule(userId, req);
            return ResponseEntity.ok(SimpleMessageResponse.builder()
                    .code(200)
                    .message("일정 추가에 성공하였습니다")
                    .build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(SimpleMessageResponse.builder()
                    .code(801)
                    .message(e.getMessage()) // "필수 입력값이 누락되었습니다. (title, alarm_time)"
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(SimpleMessageResponse.builder()
                    .code(500)
                    .message("서버 오류로 인해 일정을 추가할 수 없습니다. 잠시 후 다시 시도해주세요.")
                    .build());
        }
    }

    // 일정 수정: PUT /api/calender/{userId}/{calendarId}/schedule/{scheduleId}
    @PutMapping("/{userId}/{calendarId}/schedule/{scheduleId}")
    public ResponseEntity<?> updateSchedule(
            @PathVariable Long userId,
            @PathVariable Long calendarId,
            @PathVariable Long scheduleId,
            @RequestBody UpdateScheduleRequest req
    ) {
        try {
            ScheduleItem updated = calendarService.updateSchedule(userId, calendarId, scheduleId, req);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(SimpleMessageResponse.builder()
                    .code(801)
                    .message(e.getMessage())
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(SimpleMessageResponse.builder()
                    .code(500)
                    .message("서버 오류로 인해 일정을 추가할 수 없습니다. 잠시 후 다시 시도해주세요.")
                    .build());
        }
    }

    // 일정 삭제: DELETE /api/calender/{userId}/{calendarId}/schedule/{scheduleId}
    @DeleteMapping("/{userId}/{calendarId}/schedule/{scheduleId}")
    public ResponseEntity<?> deleteSchedule(
            @PathVariable Long userId,
            @PathVariable Long calendarId,
            @PathVariable Long scheduleId
    ) {
        try {
            calendarService.deleteSchedule(userId, calendarId, scheduleId);
            return ResponseEntity.ok(SimpleMessageResponse.builder()
                    .code(200)
                    .message("일정 삭제에 성공하였습니다.")
                    .build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(SimpleMessageResponse.builder()
                    .code(802)
                    .message("삭제할 일정이 없습니다.")
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(SimpleMessageResponse.builder()
                    .code(500)
                    .message("서버 오류로 인해 일정을 추가할 수 없습니다. 잠시 후 다시 시도해주세요.")
                    .build());
        }
    }
}
