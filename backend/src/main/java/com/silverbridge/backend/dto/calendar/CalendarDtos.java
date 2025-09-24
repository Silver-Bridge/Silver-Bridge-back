package com.silverbridge.backend.dto.calendar;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

public class CalendarDtos {

    // ── 요청 DTO ────────────────────────────────────────────────────────────────
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class CreateScheduleRequest {
        private String title;
        private String description;
        @JsonProperty("alarm_time")
        private OffsetDateTime alarmTime; // "2025-04-05T17:10:44+09:00"
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class UpdateScheduleRequest {
        @JsonProperty("schedule_id")
        private Long scheduleId; // 명세에 존재
        private String title;
        private String description;
        @JsonProperty("alarm_time")
        private OffsetDateTime alarmTime;
    }

    // ── 응답 DTO ────────────────────────────────────────────────────────────────
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class CalendarDateItem {
        private Long id;
        @JsonProperty("memberId")
        private Long memberId;
        private LocalDate date; // 응답은 "yyyy-MM-dd" 포맷으로 나감
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class CalendarDateListResponse {
        private List<CalendarDateItem> body;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ScheduleItem {
        private Long id;
        private String title;
        private String description;
        @JsonProperty("alarm_time")
        private OffsetDateTime alarmTime;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ScheduleListResponse {
        private List<ScheduleItem> body;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class SimpleMessageResponse {
        private int code;
        private String message;
    }
}
