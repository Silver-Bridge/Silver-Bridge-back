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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.security.AccessControlException;
import java.util.*;
import java.util.stream.Collectors;

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
        // 세션 조회/생성 (여기서 regionCode가 세션에 저장됨)
        ChatSession session = upsertSession(userId, req.getSessionId(), req.getRegionCode());
        List<MessageDto> history = latestHistory(session.getId(), historyLimit);

        String originalText = req.getText();
        String emotion = emotionClient.analyze(originalText);

        // 유저 메시지 저장
        saveMessage(session, ChatMessage.Role.USER, originalText, emotion);

        String contextualUserMsg = String.format("사용자 (감정: %s): %s", emotion, originalText);

        // [▼ 핵심 수정] session.getRegionCode()를 추가하여 PromptBuilder에 전달
        List<MessageDto> prompt = promptBuilder.build(
                history,
                contextualUserMsg,
                emotion,
                session.getRegionCode(), // <--- 여기 추가됨!
                seniorFriendly
        );

        // LLM 호출
        String reply = llmClient.chat(prompt, seniorFriendly);

        // 제목 생성
        generateTitleIfNeeded(session, originalText, reply);

        // 챗봇 응답 저장
        saveMessage(session, ChatMessage.Role.ASSISTANT, reply, null);

        // TTS 변환 (사투리가 섞인 텍스트를 음성으로)
        String replyAudioUrl = ttsClient.synthesize(reply, session.getRegionCode());

        List<MessageDto> updated = latestHistory(session.getId(), historyLimit);

        return ChatTextResponse.builder()
                .sessionId(session.getId())
                .history(updated)
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
        List<MessageDto> history = latestHistory(session.getId(), historyLimit);

        // [▼ 핵심 수정] session.getRegionCode()를 추가하여 PromptBuilder에 전달
        List<MessageDto> prompt = promptBuilder.build(
                history,
                contextualUserMsg,
                emotion,
                session.getRegionCode(), // <--- 여기 추가됨!
                seniorFriendly
        );

        String reply = llmClient.chat(prompt, seniorFriendly);

        generateTitleIfNeeded(session, asrText, reply);

        saveMessage(session, ChatMessage.Role.ASSISTANT, reply, null);

        String replyAudioUrl = ttsClient.synthesize(reply, session.getRegionCode());

        List<MessageDto> updatedHistory = latestHistory(session.getId(), historyLimit);

        return ChatVoiceResponse.builder()
                .sessionId(session.getId())
                .userId(userId)
                .title(session.getTitle())
                .history(updatedHistory)
                .replyAudioUrl(replyAudioUrl)
                .build();
    }

    // ... (이하 나머지 메서드는 기존과 동일) ...

    @Transactional(readOnly = true)
    public List<MessageDto> getHistory(Long userId, Long sessionId) {
        ChatSession s = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("세션 없음"));
        if (!Objects.equals(s.getUserId(), userId)) {
            throw new AccessControlException("권한 없음");
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
            throw new AccessControlException("본인 세션만 삭제할 수 있습니다.");
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
                throw new AccessControlException("권한 없음");
            }
            if (regionCode != null && !regionCode.isBlank()) {
                session.setRegionCode(regionCode);
            }
        } else {
            session = new ChatSession();
            session.setUserId(userId);
            // regionCode가 없으면 기본값 "std"(표준어) 설정
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
            if (generatedTitle.length() > 50) {
                generatedTitle = generatedTitle.substring(0, 50);
            }

            session.updateTitle(generatedTitle);
            sessionRepo.save(session);

        } catch (Exception e) {
            System.err.println("채팅방 제목 생성 실패: " + e.getMessage());
        }
    }
}