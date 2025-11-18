package com.silverbridge.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * 외부 API 호출을 위한 RestTemplate 빈 등록 설정
 */
@Configuration
public class RestTemplateConfig {

    /**
     * RestTemplate 객체를 Spring Bean으로 생성
     *
     * @return RestTemplate 싱글톤 인스턴스
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}