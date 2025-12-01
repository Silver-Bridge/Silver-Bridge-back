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

    @Value("${naver.clova.voice:nara}")
    private String defaultVoice;

    /**
     * 목소리 프로필 (화자, 볼륨, 속도, 피치)
     */
    @Getter
    @AllArgsConstructor
    private static class VoiceProfile {
        private String speaker; // 화자
        private int volume;     // 볼륨 (-5 ~ 5, 클수록 큼)
        private int speed;      // 속도 (-5 ~ 5, 양수일수록 느림)
        private int pitch;      // 피치 (-5 ~ 5, 양수일수록 저음)
    }

    /**
     * Naver Clova Voice API 호출
     */
    public String synthesize(String text, String regionCode) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }

        try {
            // 1. 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.set("X-NCP-APIGW-API-KEY-ID", clientId);
            headers.set("X-NCP-APIGW-API-KEY", clientSecret);

            // 2. 프로필 가져오기
            VoiceProfile profile = getVoiceProfile(regionCode);

            // 3. 바디 생성 (수동 인코딩 + 볼륨/속도/피치 적용)
            String encodedText = URLEncoder.encode(text, StandardCharsets.UTF_8.toString());

            // 쿼리 스트링 조립 (volume 파라미터 추가됨)
            String requestBody = String.format(
                    "speaker=%s&volume=%d&speed=%d&pitch=%d&format=mp3&text=%s",
                    profile.getSpeaker(),
                    profile.getVolume(), // 볼륨 적용
                    profile.getSpeed(),
                    profile.getPitch(),
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
     * [최종 튜닝] 어르신 맞춤형 볼륨(Volume) UP 설정
     * - Volume: 3~4 (약 1.3~1.4배 크게)
     * - Speed: 양수(+) (천천히)
     * - Pitch: 양수(+) (중저음)
     */
    private VoiceProfile getVoiceProfile(String regionCode) {
        if (regionCode == null) regionCode = "std";

        switch (regionCode.toLowerCase()) {
            case "gs": // [경상도] 무뚝뚝하지만 든든한 톤
                // 화자: nminsang (민상)
                // 볼륨: 3 (저음이라 잘 안 들릴 수 있어 가장 크게 설정)
                // 속도: 1 (약간 느림)
                // 피치: 2 (무게감 있음)
                return new VoiceProfile("nminsang", 3, 1, 2);

            case "gw": // [강원도] 순박하고 여유로운 톤
                // 화자: njinho (진호)
                // 볼륨: 3 (충분히 크게)
                // 속도: 2 (아주 느긋하게)
                // 피치: 1 (편안하게)
                return new VoiceProfile("njinho", 3, 2, 1);

            case "std": // [표준어] 또렷한 톤
            default:
                // 화자: nara (나라)
                // 볼륨: 3 (충분히 크게)
                // 속도: 2 (어르신 맞춤 느림)
                // 피치: 1 (차분함)
                return new VoiceProfile("nara", 3, 2, 1);
        }
    }
}