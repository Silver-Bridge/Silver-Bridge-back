package com.silverbridge.backend.dto.chatbot;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

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