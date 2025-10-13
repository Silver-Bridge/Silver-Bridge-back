package com.silverbridge.backend.service.chatbot;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

// 음성 인식(ASR) 서버 API 호출을 위한 클라이언트
@Component
@RequiredArgsConstructor
public class AsrClient {

    // 표준어 ASR 모델 엔드포인트
    @Value("${chatbot.asr.std-endpoint:http://localhost:9001/asr/transcribe}")
    private String stdEndpoint;

    // 경상도 ASR 모델 엔드포인트
    @Value("${chatbot.asr.gs-endpoint:http://localhost:9002/asr/transcribe}")
    private String gsEndpoint;

    // 전라도 ASR 모델 엔드포인트
    @Value("${chatbot.asr.jl-endpoint:http://localhost:9003/asr/transcribe}")
    private String jlEndpoint;

    // HTTP 통신을 위한 RestTemplate
    private final RestTemplate restTemplate = new RestTemplate();

    // 음성 파일을 ASR 서버로 보내 텍스트로 변환
    public String transcribe(String regionCode, MultipartFile file) {
        // 지역 코드에 맞는 ASR 서버 엔드포인트 선택
        String endpoint = pickEndpoint(regionCode);

        try {
            // form-data 형식으로 요청 본문 구성
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new MultipartInputResource(file));

            // multipart/form-data 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            // ASR 서버에 POST 요청 전송
            ResponseEntity<String> response =
                    restTemplate.postForEntity(endpoint, requestEntity, String.class);

            // 응답 성공 시, JSON에서 텍스트 추출 후 반환
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return parseJsonText(response.getBody());
            } else {
                return "ASR 호출 실패: " + response.getStatusCode();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "ASR 호출 중 오류 발생";
        }
    }

    // 지역 코드에 따라 적절한 엔드포인트 URL 반환
    private String pickEndpoint(String regionCode) {
        if ("gs".equalsIgnoreCase(regionCode)) return gsEndpoint;
        if ("jl".equalsIgnoreCase(regionCode)) return jlEndpoint;
        return stdEndpoint;
    }

    // ASR 서버의 JSON 응답에서 "text" 필드 값 추출
    private String parseJsonText(String body) {
        String key = "\"text\"";
        int idx = body.indexOf(key);
        if (idx == -1) return body;
        int start = body.indexOf('"', idx + key.length());
        int end = body.indexOf('"', start + 1);
        if (start != -1 && end != -1) {
            return body.substring(start + 1, end);
        }
        return body;
    }

    // RestTemplate으로 MultipartFile을 보내기 위한 Resource 래퍼 클래스
    static class MultipartInputResource extends org.springframework.core.io.AbstractResource {
        private final MultipartFile file;

        MultipartInputResource(MultipartFile file) {
            this.file = file;
        }

        @Override
        public String getDescription() {
            return "Multipart resource for " + file.getOriginalFilename();
        }

        @Override
        public java.io.InputStream getInputStream() throws java.io.IOException {
            return file.getInputStream();
        }

        @Override
        public String getFilename() {
            return file.getOriginalFilename();
        }

        @Override
        public long contentLength() throws java.io.IOException {
            return file.getSize();
        }
    }
}