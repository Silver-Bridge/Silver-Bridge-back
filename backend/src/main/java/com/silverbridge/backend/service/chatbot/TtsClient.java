package com.silverbridge.backend.service.chatbot;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;

/**
 * TTS(Text-to-Speech) API 호출 클라이언트
 * OpenAI TTS API (tts-1) 연동
 */
@Component
@RequiredArgsConstructor
public class TtsClient {

    private final RestTemplate restTemplate;

    // application.yml에서 OpenAI TTS 설정값 주입
    @Value("${chatbot.tts.openai.url}")
    private String openAiTtsUrl;

    @Value("${chatbot.tts.openai.model}")
    private String openAiTtsModel;

    // 이미 설정된 OpenAI API 키 재사용
    @Value("${spring.ai.openai.api-key}")
    private String openAiApiKey;

    /**
     * 텍스트를 OpenAI TTS API를 통해 음성으로 변환
     *
     * @param text 음성으로 변환할 텍스트
     * @param regionCode 지역 코드 (std, gs, jl)에 따라 목소리 톤 변경
     * @return Base64로 인코딩된 MP3 데이터 URI (바로 재생 가능)
     */
    public String synthesize(String text, String regionCode) {

        if (openAiApiKey == null || openAiApiKey.isBlank()) {
            return "오류: OpenAI API 키가 설정되지 않았습니다.";
        }

        try {
            // 1. HTTP 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(openAiApiKey);

            // 2. 목소리(Voice) 선택
            // (사투리 억양 자체는 미지원이나, 목소리 톤으로 화자 구분)
            String voice = pickVoice(regionCode);

            // 3. 요청 바디 생성
            OpenAiTtsRequest requestBody = new OpenAiTtsRequest(openAiTtsModel, text, voice);

            HttpEntity<OpenAiTtsRequest> requestEntity = new HttpEntity<>(requestBody, headers);

            // 4. API 호출 (응답을 바이트 배열로 수신)
            ResponseEntity<byte[]> response = restTemplate.postForEntity(openAiTtsUrl, requestEntity, byte[].class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                // 5. MP3 바이트 배열을 Base64 문자열로 변환
                String base64Audio = Base64.getEncoder().encodeToString(response.getBody());

                // 6. 프론트엔드에서 바로 재생 가능한 Data URI 반환
                return "data:audio/mp3;base64," + base64Audio;
            } else {
                return "TTS API 응답 오류: " + response.getStatusCode();
            }

        } catch (RestClientException e) {
            e.printStackTrace();
            return "TTS API 호출 실패: " + e.getMessage();
        }
    }

    /**
     * 지역 코드에 따라 OpenAI 목소리(Voice) 매핑
     * 옵션: alloy, echo, fable, onyx, nova, shimmer
     */
    private String pickVoice(String regionCode) {
        if ("gs".equalsIgnoreCase(regionCode)) {
            return "onyx"; // 경상도: 굵고 낮은 남성 톤 예시
        }
        if ("jl".equalsIgnoreCase(regionCode)) {
            return "shimmer"; // 전라도: 맑은 여성 톤 예시
        }
        return "alloy"; // 표준어: 기본 톤
    }

    // --- OpenAI TTS 요청 DTO (내부 클래스) ---
    @Data
    @AllArgsConstructor
    private static class OpenAiTtsRequest {
        private String model;
        private String input;
        private String voice;
    }
}
