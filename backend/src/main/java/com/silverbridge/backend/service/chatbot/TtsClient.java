package com.silverbridge.backend.service.chatbot;

import org.springframework.stereotype.Component;

/**
 * TTS(Text-to-Speech) API 호출을 위한 클라이언트
 * (현재는 더미 구현)
 */
@Component
public class TtsClient {

    /**
     * 텍스트를 음성으로 변환
     * (현재는 "더미" URL을 반환)
     * * @param text 음성으로 변환할 텍스트
     * @param regionCode 사투리 지역 코드 (현재 미사용)
     * @return 음성 파일에 접근할 수 있는 URL
     */
    public String synthesize(String text, String regionCode) {

        // TODO: (나중에) 실제 TTS API (OpenAI, Naver, Voiselab 등) 호출 로직 구현
        // (지금은 비용 절감을 위해 하드코딩된 더미 URL 반환)

        System.out.println("--- [Dummy TTS] 텍스트 수신: " + text.substring(0, Math.min(text.length(), 20)) + "...");
        String dummyAudioUrl = "https://example.com/audio/dummy-tts-response.mp3";
        System.out.println("--- [Dummy TTS] 더미 URL 반환: " + dummyAudioUrl + " ---");

        return dummyAudioUrl;
    }
}