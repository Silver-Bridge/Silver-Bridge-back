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

    @Getter
    @AllArgsConstructor
    private static class VoiceProfile {
        private String speaker;
        private int volume;
        private int speed;
        private int pitch;
        private Integer emotion; // null이면 감정 파라미터 제외
    }

    /**
     * Naver Clova Voice API 호출
     * @param emotionCode : 0~6 사이의 감정 코드 문자열
     */
    public String synthesize(String text, String regionCode, String gender, int age, String emotionCode) {
        if (text == null || text.trim().isEmpty()) return null;

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.set("X-NCP-APIGW-API-KEY-ID", clientId);
            headers.set("X-NCP-APIGW-API-KEY", clientSecret);

            // 1. 프로필 생성 (감정 코드 반영)
            VoiceProfile profile = getVoiceProfile(regionCode, gender, age, emotionCode);

            String encodedText = URLEncoder.encode(text, StandardCharsets.UTF_8.toString());

            // 2. 기본 파라미터 조립
            StringBuilder params = new StringBuilder();
            params.append("speaker=").append(profile.getSpeaker());
            params.append("&volume=").append(profile.getVolume());
            params.append("&speed=").append(profile.getSpeed());
            params.append("&pitch=").append(profile.getPitch());
            params.append("&format=mp3");
            params.append("&text=").append(encodedText);

            // [핵심] 지원되는 감정일 때만 파라미터 추가 (에러 방지)
            if (profile.getEmotion() != null) {
                params.append("&emotion=").append(profile.getEmotion());
                params.append("&emotion-strength=1"); // 강도는 '보통(1)'으로 고정
            }

            HttpEntity<String> requestEntity = new HttpEntity<>(params.toString(), headers);

            ResponseEntity<byte[]> response = restTemplate.postForEntity(clovaTtsUrl, requestEntity, byte[].class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                String base64Audio = Base64.getEncoder().encodeToString(response.getBody());
                return "data:audio/mp3;base64," + base64Audio;
            }

        } catch (Exception e) {
            log.error("Clova TTS API 호출 실패", e);
        }
        return null;
    }

    private VoiceProfile getVoiceProfile(String regionCode, String gender, int age, String emotionCode) {
        if (regionCode == null) regionCode = "std";

        boolean isMale = "M".equalsIgnoreCase(gender);
        boolean isOld = age >= 75;
        int ageSpeed = isOld ? 1 : 0;

        // 1. 감정 코드 매핑 (Clova가 지원하는 1, 2, 3만 반환, 나머지는 null)
        Integer targetEmotion = mapEmotion(emotionCode);

        // 2. 화자 및 기본 설정 선택
        // VoiceProfile(모델 이름, volume, speed, pitch, emotion)

        switch (regionCode.toLowerCase()) {
            case "gs": // [경상도]
                if (isMale) {
                    // 사용자가 경상도 남자 일때
                    return new VoiceProfile("nminsang", 4, 1 + ageSpeed, 2, targetEmotion);
                } else {
                    // 사용자가 경상도 여자 일때
                    return new VoiceProfile("nara", 3, 1 + ageSpeed, -1, targetEmotion);
                }

            case "gw": // [강원도]
                if (isMale) {
                    // 사용자가 강원도 남자 일때
                    return new VoiceProfile("njinho", 3, 3 + ageSpeed, 1, targetEmotion);
                } else {
                    // 사용자가 경상도 여자 일때
                    return new VoiceProfile("nara", 3, 3 + ageSpeed, 0, targetEmotion);
                }

            case "std": // [표준어]
            default:
                if (isMale) {
                    return new VoiceProfile("njinho", 3, 2 + ageSpeed, 0, targetEmotion);
                } else {
                    return new VoiceProfile("nara", 3, 2 + ageSpeed, 0, targetEmotion);
                }
        }
    }

    /**
     * [안전한 감정 매핑 로직]
     * 사용자 감정(0~6)을 Clova 감정(1,2,3)으로 변환. 지원 안 되면 null.
     */
    private Integer mapEmotion(String emotionCode) {
        if (emotionCode == null) return null;

        switch (emotionCode) {
            case "0": return 2; // 긍정(기쁨) -> Joy(2)
            case "1": return 1; // 슬픔 -> Sorrow(1)
            case "2": return 3; // 분노 -> Anger(3)
            case "3": return 1; // 불안 -> Sorrow(1) (떨리는 목소리 대용)

            // 4(놀람), 5(혐오), 6(중립)은 Clova에서 지원하지 않으므로  skip
            case "4":
            case "5":
            case "6":
            default:
                return null; // 파라미터 안 보냄 (기본 목소리)
        }
    }
}