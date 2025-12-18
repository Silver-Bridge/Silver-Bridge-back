package com.silverbridge.backend.dto.chatbot;

public class EmotionRequest {

    private String text;

    // Jackson(JSON 파서)이 사용하기 위한 기본 생성자
    public EmotionRequest() {
    }

    // 텍스트를 받는 생성자
    public EmotionRequest(String text) {
        this.text = text;
    }

    // getter
    public String getText() {
        return text;
    }

    // setter
    public void setText(String text) {
        this.text = text;
    }
}