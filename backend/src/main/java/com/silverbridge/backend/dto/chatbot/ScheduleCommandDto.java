package com.silverbridge.backend.dto.chatbot;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ScheduleCommandDto {

    // 명령의 종류를 구분하는 Enum
    public enum Action {
        ADD,    // 일정 추가
        CHECK,  // 일정 조회
        DELETE, // 일정 삭제
        ALARM,  // 알림 설정 변경
        NONE    // 일반 대화 (명령 아님)
    }

    private Action action;      // 어떤 행동인가? (ADD, CHECK, ...)
    private String title;       // 일정 제목 (예: "치과", "약국")
    private String startDateTime; // 날짜/시간 (예: "2025-11-28T14:00:00")
    private String targetDate;  // 조회용 날짜 (예: "2025-11-29")
    private Boolean alarmOn;    // 알림 켜기/끄기 값 (true/false)
}