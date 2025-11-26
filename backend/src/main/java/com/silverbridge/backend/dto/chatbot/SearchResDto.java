package com.silverbridge.backend.dto.chatbot;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchResDto {
    private String title;       // 검색 결과 제목
    private String description; // 요약 내용
    private String link;        // 바로가기 링크
}