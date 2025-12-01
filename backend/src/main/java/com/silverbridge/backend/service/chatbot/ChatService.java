package com.silverbridge.backend.service.chatbot;

import com.silverbridge.backend.dto.chatbot.ChatTextRequest;
import com.silverbridge.backend.dto.chatbot.ChatTextResponse;
import com.silverbridge.backend.dto.chatbot.ChatVoiceResponse;
import com.silverbridge.backend.dto.chatbot.MessageDto;
import com.silverbridge.backend.domain.chatbot.ChatMessage;
import com.silverbridge.backend.domain.chatbot.ChatSession;
import com.silverbridge.backend.repository.chatbot.ChatMessageRepository;
import com.silverbridge.backend.repository.chatbot.ChatSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatSessionRepository sessionRepo;
    private final ChatMessageRepository messageRepo;
    private final AsrClient asrClient;
    private final LlmClient llmClient;
    private final PromptBuilder promptBuilder;
    private final EmotionClient emotionClient;
    private final TtsClient ttsClient;

    @Value("${chatbot.senior-friendly:true}")
    private boolean seniorFriendly;

    @Value("${chatbot.history-limit:20}")
    private int historyLimit;

    // 텍스트 입력 처리
    @Transactional
    public ChatTextResponse handleText(Long userId, ChatTextRequest req) {
        ChatSession session = upsertSession(userId, req.getSessionId(), req.getRegionCode());
        List<MessageDto> history = latestHistory(session.getId(), historyLimit);

        String originalText = req.getText();
        String emotion = emotionClient.analyze(originalText);

        saveMessage(session, ChatMessage.Role.USER, originalText, emotion);
        String contextualUserMsg = String.format("사용자 (감정: %s): %s", emotion, originalText);

        List<MessageDto> prompt = promptBuilder.build(
                history, contextualUserMsg, emotion, session.getRegionCode(), seniorFriendly
        );

        String reply = llmClient.chat(prompt, seniorFriendly);
        generateTitleIfNeeded(session, originalText, reply);
        saveMessage(session, ChatMessage.Role.ASSISTANT, reply, null);

        // [수정] regionCode만 넘깁니다. (User 객체 사용 X)
        String replyAudioUrl = ttsClient.synthesize(reply, session.getRegionCode());

        return ChatTextResponse.builder()
                .sessionId(session.getId())
                .history(latestHistory(session.getId(), historyLimit))
                .replyAudioUrl(replyAudioUrl)
                .build();
    }

    // 음성 입력 처리
    @Transactional
    public ChatVoiceResponse handleVoice(Long userId, String regionCode, MultipartFile file, Long sessionId) {
        ChatSession session = upsertSession(userId, sessionId, regionCode);

        String asrText = asrClient.transcribe(session.getRegionCode(), file);
        String emotion = emotionClient.analyze(asrText);

        saveMessage(session, ChatMessage.Role.USER, asrText, emotion);
        String contextualUserMsg = String.format("사용자 (감정: %s): %s", emotion, asrText);

        List<MessageDto> prompt = promptBuilder.build(
                latestHistory(session.getId(), historyLimit),
                contextualUserMsg, emotion, session.getRegionCode(), seniorFriendly
        );

        String reply = llmClient.chat(prompt, seniorFriendly);
        generateTitleIfNeeded(session, asrText, reply);
        saveMessage(session, ChatMessage.Role.ASSISTANT, reply, null);

        // [수정] regionCode만 넘깁니다.
        String replyAudioUrl = ttsClient.synthesize(reply, session.getRegionCode());

        return ChatVoiceResponse.builder()
                .sessionId(session.getId())
                .userId(userId)
                .title(session.getTitle())
                .history(latestHistory(session.getId(), historyLimit))
                .replyAudioUrl(replyAudioUrl)
                .build();
    }

    // [이하 기존 로직 유지 + 예외 처리 수정]

    @Transactional(readOnly = true)
    public List<MessageDto> getHistory(Long userId, Long sessionId) {
        ChatSession s = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("세션 없음"));
        if (!Objects.equals(s.getUserId(), userId)) {
            // [수정] AccessControlException -> SecurityException
            throw new SecurityException("권한 없음");
        }
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
        if (!Objects.equals(session.getUserId(), userId)) {
            // [수정] AccessControlException -> SecurityException
            throw new SecurityException("본인 세션만 삭제할 수 있습니다.");
        }
        messageRepo.deleteAll(messageRepo.findTop50BySessionIdOrderByCreatedAtDesc(sessionId));
        sessionRepo.delete(session);
    }

    private ChatSession upsertSession(Long userId, Long sessionId, String regionCode) {
        ChatSession session;
        if (sessionId != null) {
            session = sessionRepo.findById(sessionId)
                    .orElseThrow(() -> new IllegalArgumentException("세션 없음"));
            if (!Objects.equals(session.getUserId(), userId)) {
                // [수정] AccessControlException -> SecurityException
                throw new SecurityException("권한 없음");
            }
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

    // ... (나머지 헬퍼 메소드들은 변경 없음) ...
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
                .map(m -> new MessageDto(
                        m.getRole().name().toLowerCase(),
                        m.getContent(),
                        m.getEmotion()
                ))
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
            log.warn("채팅방 제목 생성 실패: {}", e.getMessage());
        }
    }
}