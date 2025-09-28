package com.silverbridge.backend.service.chatbot;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@Component
@RequiredArgsConstructor
public class AsrClient {

    @Value("${chatbot.asr.std-endpoint:http://localhost:9001/asr/transcribe}")
    private String stdEndpoint;

    @Value("${chatbot.asr.gs-endpoint:http://localhost:9002/asr/transcribe}")
    private String gsEndpoint;

    @Value("${chatbot.asr.jl-endpoint:http://localhost:9003/asr/transcribe}")
    private String jlEndpoint;

    private final RestTemplate restTemplate = new RestTemplate();

    public String transcribe(String regionCode, MultipartFile file) {
        String endpoint = pickEndpoint(regionCode);

        try {
            // form-data 전송 준비
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new MultipartInputResource(file));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response =
                    restTemplate.postForEntity(endpoint, requestEntity, String.class);

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

    private String pickEndpoint(String regionCode) {
        if ("gs".equalsIgnoreCase(regionCode)) return gsEndpoint;
        if ("jl".equalsIgnoreCase(regionCode)) return jlEndpoint;
        return stdEndpoint;
    }

    /**
     * {"text":"..."} 형태 JSON에서 text 값만 뽑기
     */
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

    /**
     * MultipartFile → Resource 변환용 내부 클래스
     */
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
