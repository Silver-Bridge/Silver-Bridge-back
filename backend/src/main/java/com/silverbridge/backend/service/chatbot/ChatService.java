package com.silverbridge.backend.service.chatbot;

import com.silverbridge.backend.domain.User;
import com.silverbridge.backend.dto.calendar.CalendarDtos;
import com.silverbridge.backend.dto.chatbot.*;
import com.silverbridge.backend.domain.chatbot.ChatMessage;
import com.silverbridge.backend.domain.chatbot.ChatSession;
import com.silverbridge.backend.repository.UserRepository;
import com.silverbridge.backend.repository.chatbot.ChatMessageRepository;
import com.silverbridge.backend.repository.chatbot.ChatSessionRepository;
import com.silverbridge.backend.service.calendar.CalendarService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    // 1. 챗봇 핵심 컴포넌트
    private final ChatSessionRepository sessionRepo;
    private final ChatMessageRepository messageRepo;
    private final UserRepository userRepo;

    private final AsrClient asrClient;
    private final LlmClient llmClient;
    private final PromptBuilder promptBuilder;
    private final EmotionClient emotionClient;
    private final TtsClient ttsClient;
    private final NaverSearchClient naverSearchClient;

    // 2. 기능 수행을 위한 서비스
    private final CalendarService calendarService;

    @Value("${chatbot.senior-friendly:true}")
    private boolean seniorFriendly;

    @Value("${chatbot.history-limit:20}")
    private int historyLimit;

    // --- 1. 텍스트 입력 처리 ---
    @Transactional
    public ChatTextResponse handleText(Long userId, ChatTextRequest req) {
        return processChat(userId, req.getSessionId(), req.getRegionCode(), req.getText());
    }

    // --- 2. 음성 입력 처리 ---
    @Transactional
    public ChatVoiceResponse handleVoice(Long userId, String regionCode, MultipartFile file, Long sessionId) {
        ChatSession session = upsertSession(userId, sessionId, regionCode);
        String asrText = asrClient.transcribe(session.getRegionCode(), file);

        ChatTextResponse response = processChat(userId, session.getId(), session.getRegionCode(), asrText);

        return ChatVoiceResponse.builder()
                .userId(response.getUserId())
                .sessionId(response.getSessionId())
                .title(response.getTitle())
                .history(response.getHistory())
                .replyAudioUrl(response.getReplyAudioUrl())
                .build();
    }

    /**
     * [핵심] 공통 처리 로직 (TTS용 Raw 텍스트와 화면용 Clean 텍스트 분리)
     */
    private ChatTextResponse processChat(Long userId, Long sessionId, String regionCode, String userText) {
        // 1. 사용자 및 세션 조회
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));
        ChatSession session = upsertSession(userId, sessionId, regionCode);

        // 2. 감정 분석
        String emotion = emotionClient.analyze(userText); // 감정 분석 결과 (예: "0", "1" ...)
        saveMessage(session, ChatMessage.Role.USER, userText, emotion);

        String botReplyRaw = "";   // [TTS용] 쉼표, 말줄임표가 포함된 버전
        String botReplyClean = ""; // [화면/DB용] 깔끔하게 다듬어진 버전

        // 3. 명령 의도 파악
        ScheduleCommandDto command = llmClient.extractCommand(userText);
        log.info("🤖 감지된 명령: {}", command);

        if (command.getAction() != ScheduleCommandDto.Action.NONE) {
            // 명령 실행 결과는 보통 깔끔하므로 Raw/Clean 동일하게 처리
            botReplyClean = executeCommand(userId, command, session.getRegionCode());
            botReplyRaw = botReplyClean;
        } else {
            // [일반 대화]
            List<SearchResDto> searchResults = null;
            if (promptBuilder.isSearchNeeded(userText)) {
                searchResults = naverSearchClient.search(userText);
            }

            String contextMsg = String.format("사용자 (감정: %s): %s", emotion, userText);

            List<MessageDto> prompt = promptBuilder.build(
                    latestHistory(session.getId(), historyLimit),
                    contextMsg,
                    emotion,
                    session.getRegionCode(),
                    seniorFriendly,
                    searchResults
            );

            // LLM은 프롬프트 지시에 따라 '쉼표가 가득한' 텍스트를 줍니다.
            botReplyRaw = llmClient.chat(prompt, seniorFriendly);

            // [✨ 핵심] 화면에 보여줄 때는 쉼표/말줄임표를 청소합니다.
            botReplyClean = cleanTextForDisplay(botReplyRaw);
        }

        // 4. 제목 생성
        generateTitleIfNeeded(session, userText, botReplyClean);

        // 5. 봇 응답 저장 (Clean 버전 저장)
        saveMessage(session, ChatMessage.Role.ASSISTANT, botReplyClean, null);

        // 6. [수정됨] TTS 변환 요청 (5번째 인자로 emotion 추가!)
        String replyAudioUrl = ttsClient.synthesize(
                botReplyRaw,
                session.getRegionCode(),
                user.getGenderCode(),
                user.getAge(),
                emotion // <--- 여기가 추가되었습니다! (에러 해결)
        );

        List<MessageDto> history = latestHistory(session.getId(), historyLimit);

        return ChatTextResponse.builder()
                .userId(userId)
                .sessionId(session.getId())
                .title(session.getTitle())
                .history(history)
                .replyAudioUrl(replyAudioUrl)
                .build();
    }

    /**
     * [추가] TTS용 문장부호를 제거하여 화면용 텍스트 생성
     */
    private String cleanTextForDisplay(String rawText) {
        if (rawText == null) return "";

        return rawText
                .replace(", ", " ")
                .replace(",", " ")
                .replace("...", " ")
                .replace("..", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    /**
     * [기능 실행기]
     */
    private String executeCommand(Long userId, ScheduleCommandDto cmd, String region) {
        try {
            boolean isGyeongsang = "gs".equalsIgnoreCase(region);
            String endingQuestion = isGyeongsang ? "예?" : "요?";

            switch (cmd.getAction()) {
                case ADD:
                    if (cmd.getStartDateTime() == null) return "날짜와 시간을 정확히 말씀해 주시겠어" + endingQuestion;
                    CalendarDtos.CreateScheduleRequest req = CalendarDtos.CreateScheduleRequest.builder()
                            .title(cmd.getTitle())
                            .startAt(LocalDateTime.parse(cmd.getStartDateTime()))
                            .endAt(LocalDateTime.parse(cmd.getStartDateTime()).plusHours(1))
                            .allDay(false)
                            .alarmMinutes(10)
                            .build();
                    calendarService.addSchedule(userId, req);
                    return "일정을 등록했습니더. (" + cmd.getTitle() + ")";

                case CHECK:
                    if (cmd.getTargetDate() == null) return "언제 일정을 확인하고 싶으신가" + endingQuestion;
                    LocalDate date = LocalDate.parse(cmd.getTargetDate());
                    List<CalendarDtos.ScheduleItem> list = calendarService.getSchedules(userId, date);
                    if (list.isEmpty()) return "그날은 일정이 없네예. 푹 쉬이소.";

                    StringBuilder sb = new StringBuilder("그날 일정은 다음과 같습니더.\n");
                    for (CalendarDtos.ScheduleItem item : list) {
                        sb.append("- ").append(item.getTitle()).append("\n");
                    }
                    return sb.toString();

                case DELETE:
                    if (cmd.getTitle() == null || cmd.getTitle().isBlank()) {
                        return "어떤 일정을 지울지 말씀해 주시겠어" + endingQuestion;
                    }
                    return calendarService.deleteScheduleByTitle(userId, cmd.getTitle());

                case ALARM:
                    User user = userRepo.findById(userId)
                            .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));
                    user.setAlarmActive(cmd.getAlarmOn());
                    userRepo.save(user);
                    return cmd.getAlarmOn() ? "알림을 켰습니더." : "알림을 껐습니더. 푹 주무이소.";

                default:
                    return "죄송합니더. 제가 잘 못 알아들었네예.";
            }
        } catch (Exception e) {
            log.error("명령 실행 중 오류", e);
            return "명령을 수행하다가 문제가 좀 생겼네예. 다시 말씀해 주시겠어예?";
        }
    }

    // --- Helper Methods ---

    @Transactional(readOnly = true)
    public List<MessageDto> getHistory(Long userId, Long sessionId) {
        ChatSession s = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("세션 없음"));
        if (!Objects.equals(s.getUserId(), userId)) throw new SecurityException("권한 없음");
        return latestHistory(sessionId, Math.max(historyLimit, 50));
    }

    @Transactional(readOnly = true)
    public List<ChatSession> getSessions(Long userId) {
        return sessionRepo.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional
    public void deleteSession(Long userId, Long sessionId) {
        ChatSession session = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("세션 없음"));
        if (!Objects.equals(session.getUserId(), userId)) throw new SecurityException("본인 세션만 삭제할 수 있습니다.");
        messageRepo.deleteAll(messageRepo.findTop50BySessionIdOrderByCreatedAtDesc(sessionId));
        sessionRepo.delete(session);
    }

    private ChatSession upsertSession(Long userId, Long sessionId, String regionCode) {
        ChatSession session;
        if (sessionId != null) {
            session = sessionRepo.findById(sessionId)
                    .orElseThrow(() -> new IllegalArgumentException("세션 없음"));
            if (!Objects.equals(session.getUserId(), userId)) throw new SecurityException("권한 없음");
            if (regionCode != null && !regionCode.isBlank()) {
                session.setRegionCode(regionCode);
            }
        } else {
            session = new ChatSession();
            session.setUserId(userId);
            session.setRegionCode(regionCode == null || regionCode.isBlank() ? "std" : regionCode);
        }
        return sessionRepo.save(session);
    }

    private void saveMessage(ChatSession s, ChatMessage.Role role, String content, String emotion) {
        ChatMessage m = new ChatMessage();
        m.setSession(s);
        m.setRole(role);
        m.setContent(content);
        m.setEmotion(emotion);
        messageRepo.save(m);
    }

    private List<MessageDto> latestHistory(Long sessionId, int limit) {
        return messageRepo.findTop50BySessionIdOrderByCreatedAtDesc(sessionId).stream()
                .sorted(Comparator.comparing(ChatMessage::getCreatedAt))
                .limit(limit)
                .map(m -> new MessageDto(m.getRole().name().toLowerCase(), m.getContent(), m.getEmotion()))
                .collect(Collectors.toList());
    }

    private void generateTitleIfNeeded(ChatSession session, String userMsg, String botResponse) {
        if (session.getTitle() != null) return;
        try {
            List<MessageDto> titlePrompt = promptBuilder.buildTitlePrompt(userMsg, botResponse);
            String generatedTitle = llmClient.chat(titlePrompt, false);
            generatedTitle = generatedTitle.replace("\"", "").replace("'", "").trim();
            if (generatedTitle.length() > 50) generatedTitle = generatedTitle.substring(0, 50);
            session.updateTitle(generatedTitle);
            sessionRepo.save(session);
        } catch (Exception e) {
            log.warn("제목 생성 실패: {}", e.getMessage());
        }
    }
}