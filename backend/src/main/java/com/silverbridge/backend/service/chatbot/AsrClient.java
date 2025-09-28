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

    public String transcribe(String regionCode, MultipartFile file) {
        String endpoint = pickEndpoint(regionCode);

        // 지금은 더미 응답만 반환
        return "인식된 텍스트(더미)";
    }

    private String pickEndpoint(String regionCode) {
        if ("gs".equalsIgnoreCase(regionCode)) return gsEndpoint;
        if ("jl".equalsIgnoreCase(regionCode)) return jlEndpoint;
        return stdEndpoint;
    }
}
