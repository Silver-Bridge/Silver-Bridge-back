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

@Component
@RequiredArgsConstructor
public class EmotionClient {

    // RestTemplateConfig에서 등록된 빈 주입
    private final RestTemplate restTemplate;

    // application.yml의 "chatbot.emotion.api.endpoint" 키를 읽도록 변경
    @Value("${chatbot.emotion.api.endpoint}")
    private String emotionEndpoint;

    public String analyze(String text) {
        // 텍스트가 비어있는 경우, "중립" 반환
        if (text == null || text.isBlank()) {
            return "중립";
        }

        try {
            // HTTP 헤더 설정 (JSON 타입)
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // HTTP 본문(Body) 설정 ({"text": "..."})
            EmotionRequest requestPayload = new EmotionRequest(text);

            // 헤더와 본문을 합친 HTTP 요청 엔티티 생성
            HttpEntity<EmotionRequest> requestEntity = new HttpEntity<>(requestPayload, headers);

            // FastAPI 서버에 POST 요청 전송 및 응답 수신
            EmotionResponse response = restTemplate.postForObject(emotionEndpoint, requestEntity, EmotionResponse.class);

            // 응답 결과에서 감정 텍스트 추출
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