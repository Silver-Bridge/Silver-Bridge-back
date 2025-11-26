package com.silverbridge.backend.dto.mypage;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Setter;

public class MemberMyPageDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TextSizeUpdateRequest {
        private String textsize;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegionUpdateRequest {
        private String region;
    }

    // [▼ 추가] 알림 설정 변경 요청용 DTO
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AlarmUpdateRequest {
        private Boolean alarmActive; // true: 켜기, false: 끄기
    }
}