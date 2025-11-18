package com.silverbridge.backend.service.chatbot;

import com.silverbridge.backend.dto.chatbot.EmotionRequest;
import com.silverbridge.backend.dto.chatbot.EmotionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * 감정 분석 FastAPI 서버 API 호출 클라이언트
 */
@Component
@RequiredArgsConstructor
public class EmotionClient {

    // RestTemplateConfig에서 등록된 빈 주입
    private final RestTemplate restTemplate;

    // [수정] application.yml의 "chatbot.emotion.api.endpoint" 키를 읽도록 변경
    @Value("${chatbot.emotion.api.endpoint}")
    private String emotionEndpoint;

    /**
     * 텍스트를 감정 분석 서버로 전송하여 감정 결과를 반환
     *
     * @param text ASR(STT)을 거친 텍스트
     * @return 분석된 감정 문자열 (예: "기쁨", "슬픔")
     */
    public String analyze(String text) {
        // 텍스트가 비어있는 경우, 기본값 "중립" 반환
        if (text == null || text.isBlank()) {
            return "중립";
        }

        try {
            // 1. HTTP 헤더 설정 (JSON 타입)
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // 2. HTTP 본문(Body) 설정 ({"text": "..."})
            EmotionRequest requestPayload = new EmotionRequest(text);

            // 3. 헤더와 본문을 합친 HTTP 요청 엔티티 생성
            HttpEntity<EmotionRequest> requestEntity = new HttpEntity<>(requestPayload, headers);

            // 4. FastAPI 서버에 POST 요청 전송 및 응답 수신
            // (이제 emotionEndpoint는 "http://117.17.185.204:8001/emotion/analyze" 값을 가짐)
            EmotionResponse response = restTemplate.postForObject(emotionEndpoint, requestEntity, EmotionResponse.class);

            // 5. 응답 결과에서 감정 텍스트 추출
            if (response != null && response.getEmotion() != null) {
                return response.getEmotion();
            } else {
                // 응답이 비정상일 경우 "분석실패" 반환
                return "분석실패";
            }

        } catch (RestClientException e) {
            // API 호출 중 네트워크 오류 등 발생 시
            e.printStackTrace();
            return "오류"; // 오류 발생 시 "오류" 반환
        }
    }
}