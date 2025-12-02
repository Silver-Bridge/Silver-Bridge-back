package com.silverbridge.backend.service.chatbot;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

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
    @Builder
    private static class VoiceProfile {
        private String speaker; // 화자 코드 (예: nsangdo, vgoeun)
        private int volume;     // -5 ~ 5 (0: 기본)
        private int speed;      // -5(빠름) ~ 5(느림) *주의: 양수가 느리게
        private int pitch;      // -5(높음) ~ 5(낮음) *주의: 음수는 높게
        private int alpha;      // -5 ~ 5 (음색: 낮을수록 중후함/남성적)
        private Integer emotion; // 0~3 (지원 안하면 null)
    }

    /**
     * Naver Clova Voice API 호출
     */
    public String synthesize(String text, String regionCode, String gender, int age, String emotionCode) {
        if (text == null || text.trim().isEmpty()) return null;

        try {
            // 1. 프로필 생성 (사용자 정보 + 감정 + 사투리 튜닝)
            VoiceProfile profile = getVoiceProfile(regionCode, gender, age, emotionCode);

            // 2. 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.set("X-NCP-APIGW-API-KEY-ID", clientId);
            headers.set("X-NCP-APIGW-API-KEY", clientSecret);

            // 3. 파라미터 조립 (MultiValueMap 사용 권장)
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("speaker", profile.getSpeaker());
            params.add("volume", String.valueOf(profile.getVolume()));
            params.add("speed", String.valueOf(profile.getSpeed()));
            params.add("pitch", String.valueOf(profile.getPitch()));
            params.add("alpha", String.valueOf(profile.getAlpha())); // 음색 추가
            params.add("format", "mp3");
            params.add("text", text); // RestTemplate이 자동으로 인코딩 처리해주기도 함 (FormHttpMessageConverter)

            // [핵심] 감정 지원 보이스인 경우에만 파라미터 추가
            if (profile.getEmotion() != null) {
                params.add("emotion", String.valueOf(profile.getEmotion()));
                params.add("emotion-strength", "1"); // 0(약함), 1(보통), 2(강함)
            }

            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(params, headers);
            ResponseEntity<byte[]> response = restTemplate.postForEntity(clovaTtsUrl, requestEntity, byte[].class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                String base64Audio = Base64.getEncoder().encodeToString(response.getBody());
                return "data:audio/mp3;base64," + base64Audio;
            }

        } catch (Exception e) {
            log.error("Clova TTS API 호출 실패: {}", e.getMessage());
        }
        return null;
    }

    /**
     * [보이스 프로필 결정 로직]
     * 상도, 대성, 고은, 유나 등을 조합하여 최적의 목소리 세팅값 반환
     */
    private VoiceProfile getVoiceProfile(String regionCode, String gender, int age, String emotionCode) {
        if (regionCode == null) regionCode = "std";

        boolean isMale = "M".equalsIgnoreCase(gender);
        boolean isOld = age >= 70; // 70세 이상을 어르신으로 간주

        // 1. 기본 속도 & 톤 설정 (나이가 많을수록 천천히, 중후하게)
        int baseSpeed = isOld ?  1 : 0;  // 2: 꽤 느림 (어르신 듣기 편함)
        int baseAlpha = isOld ? -1 : 0; // -1: 약간 낮은 톤 (신뢰감, 어른스러움)

        // 2. 감정 매핑 (지원 안하는 보이스는 나중에 null 처리됨)
        Integer targetEmotion = mapEmotion(emotionCode);

        // --------------------------------------------------------
        // A. 경상도 (GS) - 상도(Native) 위주
        // --------------------------------------------------------
        if ("gs".equalsIgnoreCase(regionCode)) {
            if (isMale) {
                // [디폴트 남자] 상도 (감정 미지원, 구수한 사투리)
                // 상도는 이미 말이 좀 빠를 수 있어서 속도를 1~2로 늦춤
                return VoiceProfile.builder()
                        .speaker("nsangdo")
                        .volume(3)
                        .speed(baseSpeed)
                        .pitch(0)
                        .alpha(0) // 상도는 원본 유지가 베스트
                        .emotion(null) // 상도는 감정 파라미터 지원 X
                        .build();
            } else {
                // [경상도 여자] 사투리 여자 보이스가 없으므로 '고은(Pro)'을 튜닝
                // Pitch를 1(약간 높음)로 주어 억양을 살림
                return VoiceProfile.builder()
                        .speaker("vgoeun") // 고은 (Pro)
                        .volume(3)
                        .speed(baseSpeed)
                        .pitch(1)
                        .alpha(baseAlpha)
                        .emotion(targetEmotion) // 감정 지원 O
                        .build();
            }
        }

        // --------------------------------------------------------
        // B. 강원도 (GW) - 순박하고 느린 느낌
        // --------------------------------------------------------
        if ("gw".equalsIgnoreCase(regionCode)) {
            if (isMale) {
                // [강원도 남자] 동현 (Pro) - 신뢰감 + 느리게
                return VoiceProfile.builder()
                        .speaker("ndonghyun") // 동현 (Pro)
                        .volume(3)
                        .speed(baseSpeed + 1) // 강원도는 조금 더 여유있게 (+1)
                        .pitch(0)
                        .alpha(baseAlpha)
                        .emotion(null) // 동현 Pro 여부 확인 필요(일반 ndonghyun은 감정X, vdonghyun은 O)
                        // *목록상 ndonghyun 사용 -> 감정 null 처리 안전
                        .build();
            } else {
                // [강원도 여자] 유나 (Pro) - 활기차지만 착한 손녀 느낌
                return VoiceProfile.builder()
                        .speaker("vyuna")
                        .volume(3)
                        .speed(baseSpeed + 1)
                        .pitch(0)
                        .alpha(0)
                        .emotion(targetEmotion)
                        .build();
            }
        }

        // --------------------------------------------------------
        // C. 표준어/기타 (STD) - 신뢰감 있는 '대성', '고은'
        // --------------------------------------------------------
        // isMale -> 대성(Pro)
        if (isMale) {
            return VoiceProfile.builder()
                    .speaker("vdaeseong") // 대성 (Pro) - 감정 지원 O
                    .volume(4) // 목소리가 부드러워서 볼륨 약간 Up
                    .speed(baseSpeed)
                    .pitch(0)
                    .alpha(baseAlpha)
                    .emotion(targetEmotion)
                    .build();
        } else {
            // isFemale -> 고은(Pro)
            return VoiceProfile.builder()
                    .speaker("vgoeun") // 고은 (Pro)
                    .volume(3)
                    .speed(baseSpeed)
                    .pitch(0)
                    .alpha(0)
                    .emotion(targetEmotion)
                    .build();
        }
    }

    /**
     * 감정 코드 매핑
     * API 지원: 0(중립), 1(슬픔), 2(기쁨), 3(분노)
     */
    private Integer mapEmotion(String emotionCode) {
        if (emotionCode == null) return null;

        switch (emotionCode) {
            case "0": return 2; // 기쁨 -> Joy(2)
            case "1": return 1; // 슬픔 -> Sorrow(1)
            case "2": return 3; // 화남 -> Anger(3)
            case "3": return 1; // 불안 -> 떨림 효과가 없으므로 슬픔(1)으로 대체
            default: return 0;  // 그 외(평온 등) -> 중립(0)
        }
    }
}