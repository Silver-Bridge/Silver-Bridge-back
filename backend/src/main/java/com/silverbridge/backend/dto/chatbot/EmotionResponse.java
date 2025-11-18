package com.silverbridge.backend.dto.chatbot;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 감정 분석 서버 응답용 DTO ({"emotion": ...})
 * FastAPI 서버로부터 받은 JSON 응답을 매핑
 */
// DTO에 정의되지 않은 JSON 속성은 무시
@JsonIgnoreProperties(ignoreUnknown = true)
public class EmotionResponse {

    private String emotion;

    // Jackson(JSON 파서)이 사용하기 위한 기본 생성자
    public EmotionResponse() {
    }

    // getter
    public String getEmotion() {
        return emotion;
    }

    // setter
    public void setEmotion(String emotion) {
        this.emotion = emotion;
    }
}