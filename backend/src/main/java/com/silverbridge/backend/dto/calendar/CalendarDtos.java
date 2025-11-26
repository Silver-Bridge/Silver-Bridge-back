package com.silverbridge.backend.dto.calendar;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.silverbridge.backend.domain.calendar.CalendarEvent;
import lombok.*;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

public class CalendarDtos {

    // 요청 DTO
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateScheduleRequest {
        private String title;
        private String description;
        @JsonProperty("start_at")
        private LocalDateTime startAt;
        @JsonProperty("end_at")
        private LocalDateTime endAt;
        @JsonProperty("all_day")
        private Boolean allDay;
        private String location;
        @JsonProperty("repeat_type")
        private CalendarEvent.RepeatType repeatType;
        @JsonProperty("priority")
        private CalendarEvent.Priority priority;
        @JsonProperty("alarm_time")
        private OffsetDateTime alarmTime;
        @JsonProperty("alarm_minutes")
        private Integer alarmMinutes;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdateScheduleRequest {
        @JsonProperty("schedule_id")
        private Long scheduleId;
        private String title;
        private String description;
        @JsonProperty("start_at")
        private LocalDateTime startAt;
        @JsonProperty("end_at")
        private LocalDateTime endAt;
        @JsonProperty("all_day")
        private Boolean allDay;
        private String location;
        @JsonProperty("repeat_type")
        private CalendarEvent.RepeatType repeatType;
        @JsonProperty("priority")
        private CalendarEvent.Priority priority;
        @JsonProperty("alarm_time")
        private OffsetDateTime alarmTime;
        @JsonProperty("alarm_minutes")
        private Integer alarmMinutes;

        // [수정] 완료 여부도 수정 가능하도록 필드 추가
        @JsonProperty("is_completed")
        private Boolean isCompleted;
    }

    // 응답 DTO
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CalendarDateItem {
        private Long id;
        @JsonProperty("memberId")
        private Long memberId;
        private LocalDate date;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CalendarDateListResponse {
        private List<CalendarDateItem> body;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ScheduleItem {
        private Long id;
        private String title;
        private String description;
        @JsonProperty("start_at")
        private LocalDateTime startAt;
        @JsonProperty("end_at")
        private LocalDateTime endAt;
        @JsonProperty("all_day")
        private Boolean allDay;
        private String location;
        @JsonProperty("repeat_type")
        private CalendarEvent.RepeatType repeatType;
        @JsonProperty("priority")
        private CalendarEvent.Priority priority;
        @JsonProperty("alarm_time")
        private LocalDateTime alarmTime;
        @JsonProperty("alarm_minutes")
        private Integer alarmMinutes;

        // [핵심] 이 필드가 있어야 에러가 사라집니다!
        @JsonProperty("is_completed")
        private Boolean isCompleted;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ScheduleListResponse {
        private List<ScheduleItem> body;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SimpleMessageResponse {
        private int code;
        private String message;
    }
}