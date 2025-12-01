package com.silverbridge.backend.service.chatbot;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Slf4j
@Component
@RequiredArgsConstructor
public class TtsClient {

    private final RestTemplate restTemplate;

    @Value("${naver.clova.url}")
    private String clovaTtsUrl;

    @Value("${naver.clova.client-id}")
    private String clientId;

    @Value("${naver.clova.client-secret}")
    private String clientSecret;

    /**
     * 목소리 프로필 (화자, 볼륨, 속도, 피치, 감정)
     */
    @Getter
    @AllArgsConstructor
    private static class VoiceProfile {
        private String speaker;
        private int volume;      // -5 ~ 5
        private int speed;       // -5 ~ 5 (양수일수록 느림)
        private int pitch;       // -5 ~ 5 (양수일수록 저음)
        private int emotion;     // 0:중립, 1:슬픔, 2:기쁨, 3:화남
        private int emotionStrength; // 0:약함, 1:보통, 2:강함
    }

    /**
     * Naver Clova Voice API 호출 (성별, 나이 반영)
     * 파라미터 4개를 받도록 수정됨
     */
    public String synthesize(String text, String regionCode, String gender, int age) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }

        try {
            // 1. 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.set("X-NCP-APIGW-API-KEY-ID", clientId);
            headers.set("X-NCP-APIGW-API-KEY", clientSecret);

            // 2. 프로필 가져오기 (지역, 성별, 나이 반영)
            VoiceProfile profile = getVoiceProfile(regionCode, gender, age);

            // 3. 바디 생성 (수동 인코딩 + 모든 튜닝값 적용)
            String encodedText = URLEncoder.encode(text, StandardCharsets.UTF_8.toString());

            String requestBody = String.format(
                    "speaker=%s&volume=%d&speed=%d&pitch=%d&emotion=%d&emotion-strength=%d&format=mp3&text=%s",
                    profile.getSpeaker(),
                    profile.getVolume(),
                    profile.getSpeed(),
                    profile.getPitch(),
                    profile.getEmotion(),
                    profile.getEmotionStrength(),
                    encodedText
            );

            HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);

            // 4. API 호출
            ResponseEntity<byte[]> response = restTemplate.postForEntity(clovaTtsUrl, requestEntity, byte[].class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                byte[] data = response.getBody();
                String base64Audio = Base64.getEncoder().encodeToString(data);
                return "data:audio/mp3;base64," + base64Audio;
            }

        } catch (Exception e) {
            log.error("Clova TTS API 호출 실패", e);
        }
        return null;
    }

    /**
     * [최종 튜닝 로직]
     * 1. 지역별 사투리 특색 (감정 섞기)
     * 2. 성별에 따른 화자 분기 (남:민상/진호, 여:나라)
     * 3. 나이에 따른 속도 조절 (75세 이상은 더 천천히)
     * 4. 난청 고려한 볼륨/피치 최적화
     */
    private VoiceProfile getVoiceProfile(String regionCode, String gender, int age) {
        if (regionCode == null) regionCode = "std";

        // 성별 기본값 처리 (없으면 여성)
        boolean isMale = "M".equalsIgnoreCase(gender);

        // 75세 이상 고령자 체크
        boolean isOld = age >= 75;
        int baseSpeed = isOld ? 1 : 0; // 고령자면 기본적으로 1단계 더 느리게

        switch (regionCode.toLowerCase()) {
            case "gs": // [경상도] -> 화남(Anger) 감정을 섞어 단호한 톤 연출
                if (isMale) {
                    // 남성: 민상 (Volume 4로 증폭, 저음 강조)
                    return new VoiceProfile("nminsang", 4, 1 + baseSpeed, 2, 3, 1);
                } else {
                    // 여성: 나라 (Volume 3, 톤 다운)
                    return new VoiceProfile("nara", 3, 1 + baseSpeed, -1, 3, 1);
                }

            case "gw": // [강원도] -> 슬픔(Sorrow) 감정을 섞어 나긋나긋한 톤 연출
                if (isMale) {
                    // 남성: 진호 (Volume 3, 아주 느리게)
                    return new VoiceProfile("njinho", 3, 2 + baseSpeed, 1, 1, 1);
                } else {
                    // 여성: 나라 (Volume 3, 아주 느리게, 기본 톤)
                    return new VoiceProfile("nara", 3, 2 + baseSpeed, 0, 1, 1);
                }

            case "std": // [표준어] -> 기쁨(Joy) 감정을 섞어 친절한 톤 연출
            default:
                if (isMale) {
                    // 남성: 진호 (표준 속도)
                    return new VoiceProfile("njinho", 3, 1 + baseSpeed, 0, 2, 1);
                } else {
                    // 여성: 나라 (표준 속도)
                    return new VoiceProfile("nara", 3, 1 + baseSpeed, 0, 2, 1);
                }
        }
    }
}