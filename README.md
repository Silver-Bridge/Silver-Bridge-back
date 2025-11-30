# Silver Bridge Backend
> 지역별 노인 맞춤형 사투리 음성인식 서비스를 제공하는 AI 기반 복지 플랫폼 **Silver Bridge**의 백엔드 서버입니다.

---

# 📚 Table of Contents

1. [프로젝트 구조](#-1-프로젝트-구조)
2. [Controller & API Summary](#-2-controller--api-summary)
3. [Service Layer Summary](#-3-service-layer-summary)
4. [Build & Run](#-4-build--run)


---

# 🚀 프로젝트 개요
Silver Bridge Backend는 고령층의 디지털 소외를 해소하기 위해  
**사투리 기반 음성인식(STT), 감정 분석, TTS, 일정관리, 보호자 연동 기능** 등을 제공하는 Spring Boot 기반 서버입니다.

---

# ⚙️ 기술 스택
- **Java 17**
- **Spring Boot 3.x**
- **Gradle**
- **Spring Security + JWT**
- **JPA/Hibernate**
- **MariaDB**
- **AWS (EC2, Nginx)**
- **FastAPI (ASR/STT 서버)**

---

# 📁 1. 프로젝트 구조
<pre>
└── 📁 backend
    ├── 📁 __pycache__
    │   ├── 📄 asr_server.cpython-313.pyc
    │   ├── 📄 asr_server_dummy.cpython-313.pyc
    │   ├── 📄 emotion_server.cpython-313.pyc
    │   └── 📄 emotion_server_dummy.cpython-313.pyc
    ├── 📄 asr_server.py
    ├── 📄 build.gradle.kts
    ├── 📄 docker-compose.yml
    ├── 📄 emotion_server.py
    ├── 📄 settings.gradle.kts
    └── 📁 src
        ├── 📁 main
        │   ├── 📁 java
        │   │   └── 📁 com
        │   │       └── 📁 silverbridge
        │   │           └── 📁 backend
        │   │               ├── 📄 BackendApplication.java
        │   │               ├── 📁 config
        │   │               │   ├── 📄 AppConfig.java
        │   │               │   ├── 📄 ChatConfig.java
        │   │               │   ├── 📄 RestTemplateConfig.java
        │   │               │   ├── 📄 SecurityConfig.java
        │   │               │   └── 📄 WebClientConfig.java
        │   │               ├── 📁 controller
        │   │               │   ├── 📄 EmotionController.java
        │   │               │   ├── 📄 HealthCheckController.java
        │   │               │   ├── 📄 KakaoAuthController.java
        │   │               │   ├── 📄 SmsController.java
        │   │               │   ├── 📄 SocialInfoController.java
        │   │               │   ├── 📄 UserController.java
        │   │               │   ├── 📁 calendar
        │   │               │   │   └── 📄 CalendarController.java
        │   │               │   ├── 📁 chatbot
        │   │               │   │   └── 📄 ChatController.java
        │   │               │   ├── 📁 guardian
        │   │               │   │   └── 📄 GuardianController.java
        │   │               │   └── 📁 mypage
        │   │               │       ├── 📄 MemberMyPageController.java
        │   │               │       └── 📄 MyPageCommonController.java
        │   │               ├── 📄 docker-compose.yml
        │   │               ├── 📁 domain
        │   │               │   ├── 📄 RefreshToken.java
        │   │               │   ├── 📄 User.java
        │   │               │   ├── 📁 calendar
        │   │               │   │   └── 📄 CalendarEvent.java
        │   │               │   └── 📁 chatbot
        │   │               │       ├── 📄 ChatMessage.java
        │   │               │       └── 📄 ChatSession.java
        │   │               ├── 📁 dto
        │   │               │   ├── 📄 EmotionCountDto.java
        │   │               │   ├── 📄 EmotionCountProjection.java
        │   │               │   ├── 📄 FinalRegisterRequest.java
        │   │               │   ├── 📄 JoinRequest.java
        │   │               │   ├── 📄 KakaoProfile.java
        │   │               │   ├── 📄 KakaoTokenResponse.java
        │   │               │   ├── 📄 LoginRequest.java
        │   │               │   ├── 📄 LogoutRequest.java
        │   │               │   ├── 📄 TokenDto.java
        │   │               │   ├── 📄 UserResponse.java
        │   │               │   ├── 📁 calendar
        │   │               │   │   └── 📄 CalendarDtos.java
        │   │               │   ├── 📁 chatbot
        │   │               │   │   ├── 📄 ChatTextRequest.java
        │   │               │   │   ├── 📄 ChatTextResponse.java
        │   │               │   │   ├── 📄 ChatVoiceResponse.java
        │   │               │   │   ├── 📄 EmotionRequest.java
        │   │               │   │   ├── 📄 EmotionResponse.java
        │   │               │   │   ├── 📄 MessageDto.java
        │   │               │   │   ├── 📄 ScheduleCommandDto.java
        │   │               │   │   └── 📄 SearchResDto.java
        │   │               │   ├── 📁 guardian
        │   │               │   │   └── 📄 ConnectRequest.java
        │   │               │   └── 📁 mypage
        │   │               │       ├── 📄 MemberMyPageDto.java
        │   │               │       └── 📄 MyPageDto.java
        │   │               ├── 📁 exception
        │   │               │   └── 📄 CustomException.java
        │   │               ├── 📁 handler
        │   │               │   └── 📄 GlobalExceptionHandler.java
        │   │               ├── 📁 jwt
        │   │               │   ├── 📄 JwtAuthenticationFilter.java
        │   │               │   └── 📄 JwtTokenProvider.java
        │   │               ├── 📁 repository
        │   │               │   ├── 📄 EmotionRepository.java
        │   │               │   ├── 📄 RefreshTokenRepository.java
        │   │               │   ├── 📄 UserRepository.java
        │   │               │   ├── 📁 calendar
        │   │               │   │   └── 📄 CalendarEventRepository.java
        │   │               │   └── 📁 chatbot
        │   │               │       ├── 📄 ChatMessageRepository.java
        │   │               │       └── 📄 ChatSessionRepository.java
        │   │               └── 📁 service
        │   │                   ├── 📄 ConnectElderService.java
        │   │                   ├── 📄 CustomUserDetailsService.java
        │   │                   ├── 📄 EmotionService.java
        │   │                   ├── 📄 KakaoService.java
        │   │                   ├── 📄 SmsService.java
        │   │                   ├── 📄 SmsVerificationManager.java
        │   │                   ├── 📄 TokenCleanupService.java
        │   │                   ├── 📄 UserService.java
        │   │                   ├── 📁 calendar
        │   │                   │   ├── 📄 CalendarService.java
        │   │                   │   ├── 📄 CalendarServiceImpl.java
        │   │                   │   ├── 📄 ElderAccessService.java
        │   │                   │   └── 📄 NotificationScheduler.java
        │   │                   ├── 📁 chatbot
        │   │                   │   ├── 📄 AsrClient.java
        │   │                   │   ├── 📄 ChatService.java
        │   │                   │   ├── 📄 EmotionClient.java
        │   │                   │   ├── 📄 LlmClient.java
        │   │                   │   ├── 📄 NaverSearchClient.java
        │   │                   │   ├── 📄 PromptBuilder.java
        │   │                   │   └── 📄 TtsClient.java
        │   │                   └── 📁 mypage
        │   │                       ├── 📄 MemberMyPageService.java
        │   │                       └── 📄 MyPageService.java
        │   └── 📁 resources
        │       ├── 📄 application.yml
        │       └── 📁 db
        │           └── 📁 migration
        │               ├── 📄 V2_create_calendars.sql
        │               └── 📄 V3_create_schedules.sql
        └── 📁 test
            └── 📁 java
                └── 📁 com
                    └── 📁 silverbridge
                        └── 📁 backend
                            └── 📄 BackendApplicationTests.java

</pre>

---

# 🧭 2. Controller & API Summary
No Controllers found or parsed.

---

# 🧩 3. Service Layer Summary
No Services found.

---

# 🛠 4. Build & Run
```bash
./gradlew clean build
java -jar build/libs/silverbridge-backend.jar
# Test
./gradlew test
```

> **Last Updated:** 2025-11-30 10:52:15
