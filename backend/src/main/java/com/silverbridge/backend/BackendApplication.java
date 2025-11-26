package com.silverbridge.backend;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.annotation.PostConstruct; // [추가] Spring Boot 3.x
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone; // [추가] 시간대 설정을 위해 필요

@EnableScheduling
@SpringBootApplication
public class BackendApplication {

    // [추가] 1. 서버 켜지자마자 시간대를 '한국(Seoul)'로 고정
    @PostConstruct
    public void started() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
        System.out.println("✅ 현재 서버 시간(KST) 설정 완료: " + new java.util.Date());
    }

    public static void main(String[] args) {

        // [기존] 2. .env 파일 로드해서 시스템 환경변수로 설정
        // (배포 시 파일이 없어도 에러 안 나게 하려면 ignoreIfMissing()을 붙이는 게 좋습니다)
        try {
            Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
            dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));
        } catch (Exception e) {
            System.out.println("⚠️ .env 파일 로드 건너뜀 (환경변수 설정이 되어있다면 무시하세요)");
        }

        SpringApplication.run(BackendApplication.class, args);
    }
}