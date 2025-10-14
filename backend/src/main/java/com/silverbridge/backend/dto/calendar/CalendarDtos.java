package com.silverbridge.backend.dto.calendar;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.silverbridge.backend.domain.calendar.CalendarEvent;
import lombok.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

// 캘린더 기능 관련 DTO를 모아놓은 클래스
public class CalendarDtos {

    // 요청 DTO

    // 일정 생성을 위한 요청 DTO
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateScheduleRequest {
        // 일정 제목
        private String title;
        // 상세 설명
        private String description;
        // 시작 시간 (타임존 포함)
        @JsonProperty("start_at")
        private OffsetDateTime startAt;
        // 종료 시간 (타임존 포함)
        @JsonProperty("end_at")
        private OffsetDateTime endAt;
        // 하루 종일 여부
        @JsonProperty("all_day")
        private Boolean allDay;
        // 장소
        private String location;
        // 반복 설정
        @JsonProperty("repeat_type")
        private CalendarEvent.RepeatType repeatType;
        // 중요도
        @JsonProperty("priority")
        private CalendarEvent.Priority priority;
        // 알림 시간
        @JsonProperty("alarm_time")
        private OffsetDateTime alarmTime;
    }

    // 일정 수정을 위한 요청 DTO
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdateScheduleRequest {
        // 수정할 일정의 ID
        @JsonProperty("schedule_id")
        private Long scheduleId;
        // 일정 제목
        private String title;
        // 상세 설명
        private String description;
        // 시작 시간 (타임존 포함)
        @JsonProperty("start_at")
        private OffsetDateTime startAt;
        // 종료 시간 (타임존 포함)
        @JsonProperty("end_at")
        private OffsetDateTime endAt;
        // 하루 종일 여부
        @JsonProperty("all_day")
        private Boolean allDay;
        // 장소
        private String location;
        // 반복 설정
        @JsonProperty("repeat_type")
        private CalendarEvent.RepeatType repeatType;
        // 중요도
        @JsonProperty("priority")
        private CalendarEvent.Priority priority;
        // 알림 시간
        @JsonProperty("alarm_time")
        private OffsetDateTime alarmTime;
    }

    // 응답 DTO

    // 월별 캘린더 조회 시, 일정이 있는 날짜 정보를 담는 DTO
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CalendarDateItem {
        // 일정 ID
        private Long id;
        // 사용자 ID
        @JsonProperty("memberId")
        private Long memberId;
        // 일정이 존재하는 날짜
        private LocalDate date;
    }

    // 월별 캘린더 조회 응답 DTO
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CalendarDateListResponse {
        // 날짜별 일정 정보 리스트
        private List<CalendarDateItem> body;
    }

    // 특정 날짜의 상세 일정 정보를 담는 DTO
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ScheduleItem {
        // 일정 ID
        private Long id;
        // 일정 제목
        private String title;
        // 상세 설명
        private String description;
        // 시작 시간 (타임존 포함)
        @JsonProperty("start_at")
        private OffsetDateTime startAt;
        // 종료 시간 (타임존 포함)
        @JsonProperty("end_at")
        private OffsetDateTime endAt;
        // 하루 종일 여부
        @JsonProperty("all_day")
        private Boolean allDay;
        // 장소
        private String location;
        // 반복 설정
        @JsonProperty("repeat_type")
        private CalendarEvent.RepeatType repeatType;
        // 중요도
        @JsonProperty("priority")
        private CalendarEvent.Priority priority;
        // 알림 시간
        @JsonProperty("alarm_time")
        private OffsetDateTime alarmTime;
    }

    // 상세 일정 목록 조회 응답 DTO
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ScheduleListResponse {
        // 상세 일정 정보 리스트
        private List<ScheduleItem> body;
    }

    // 성공/실패 등 간단한 메시지 응답을 위한 DTO
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SimpleMessageResponse {
        // 응답 코드
        private int code;
        // 응답 메시지
        private String message;
    }
}