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
- **AWS (EC2, S3, Route53, Nginx)**
- **FastAPI (ASR/STT 서버)**

---

# 📁 1. 프로젝트 구조
<pre>
└── 📁 backend
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
### 📌 EmotionController
- **RequestMapping** (/api/emotions)
- **GetMapping** (/today/top)
- **GetMapping** (/weekly/last)
- **GetMapping** (/month/current)
- **GetMapping** (/month/previous)

### 📌 SocialInfoController
- **RequestMapping** (/api/users/social)
- **PostMapping** (/register-final)

### 📌 KakaoAuthController
- **RequestMapping** (/api/users/social)
- **PostMapping** (/kakao)

### 📌 SmsController
- **RequestMapping** (/api/sms)
- **PostMapping** (/send)
- **PostMapping** (/verify)

### 📌 UserController
- **RequestMapping** (/api/users)
- **PostMapping** (/join)
- **PostMapping** (/login)
- **PostMapping** (/logout)
- **GetMapping** (/me)

### 📌 HealthCheckController
- **RequestMapping** (/api/health)
- **GetMapping** 

### 📌 CalendarController
- **RequestMapping** (/api/calendar)
- **GetMapping** 
- **GetMapping** (/schedules)
- **PostMapping** (/add)
- **PutMapping** (/schedule/{scheduleId})
- **DeleteMapping** (/schedule/{scheduleId})
- **PatchMapping** (/schedule/{scheduleId}/complete)
- **GetMapping** (/alarm/check)

### 📌 MemberMyPageController
- **RequestMapping** (/api/mypage/member)
- **PatchMapping** (/text-size)
- **PatchMapping** (/region)
- **PatchMapping** (/alarm)
- **GetMapping** (/guardian-info)

### 📌 MyPageCommonController
- **RequestMapping** (/api/mypage)
- **PatchMapping** (/password-update)
- **PatchMapping** (/alarm)

### 📌 GuardianController
- **RequestMapping** (/api/mypage/nok)
- **PostMapping** (/connect)
- **GetMapping** (/elder-info)

### 📌 ChatController
- **RequestMapping** (/api/chatbot)
- **PostMapping** (/text)
- **PostMapping** (value = /voice, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
- **GetMapping** (/history/{sessionId})
- **GetMapping** (/sessions)
- **DeleteMapping** (/session/{sessionId})



---

# 🧩 3. Service Layer Summary
### 🧩 TokenCleanupService
- `cleanupExpiredTokens()`

### 🧩 EmotionService
- `getLastWeekEmotionSummary()`
- `getEmotionSummaryMonthly()`
- `getTodayTopEmotion()`

### 🧩 KakaoService
- `getKakaoAccessToken()`
- `getKakaoProfile()`

### 🧩 SmsService
- `sendVerificationCode()`

### 🧩 CustomUserDetailsService
- `loadUserByUsername()`

### 🧩 UserService
- `join()`
- `socialLoginOrJoin()`
- `completeSocialRegistration()`
- `generateTokens()`
- `generateTokens()`
- `logout()`
- `getUserInfo()`
- `findByPhoneNumber()`

### 🧩 ConnectElderService
- `connectElder()`

### 🧩 ElderAccessService
- `getAccessibleElderId()`

### 🧩 CalendarServiceImpl
- `getCalendarDates()`
- `getSchedules()`
- `addSchedule()`
- `updateSchedule()`
- `deleteSchedule()`
- `deleteScheduleByTitle()`
- `toggleScheduleCompletion()`
- `checkAlarm()`

### 🧩 MemberMyPageService
- `updateTextSize()`
- `updateRegion()`

### 🧩 MyPageService
- `updatePassword()`
- `updateAlarm()`

### 🧩 ChatService
- `handleText()`
- `handleVoice()`
- `getHistory()`
- `getSessions()`
- `deleteSession()`



---

# 🛠 4. Build & Run
```bash
cd backend
./gradlew clean build
java -jar build/libs/silverbridge-backend.jar
# Test
./gradlew test
```

> **Last Updated:** 2025-11-30 10:58:05
