package com.silverbridge.backend.service.chatbot;

import com.silverbridge.backend.dto.chatbot.ChatTextRequest;
import com.silverbridge.backend.dto.chatbot.ChatTextResponse;
import com.silverbridge.backend.dto.chatbot.ChatVoiceResponse;
import com.silverbridge.backend.dto.chatbot.MessageDto;
import com.silverbridge.backend.domain.chatbot.ChatMessage;
import com.silverbridge.backend.domain.chatbot.ChatSession;
import com.silverbridge.backend.repository.chatbot.ChatMessageRepository;
import com.silverbridge.backend.repository.chatbot.ChatSessionRepository;
import com.silverbridge.backend.dto.chatbot.SearchResDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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
    private final NaverSearchClient naverSearchClient; // 검색 클라이언트 포함

    @Value("${chatbot.senior-friendly:true}")
    private boolean seniorFriendly;

    @Value("${chatbot.history-limit:20}")
    private int historyLimit;

    // --- 1. 텍스트 입력 처리 ---
    @Transactional
    public ChatTextResponse handleText(Long userId, ChatTextRequest req) {
        // 세션 조회 및 지역 코드 설정
        ChatSession session = upsertSession(userId, req.getSessionId(), req.getRegionCode());
        List<MessageDto> history = latestHistory(session.getId(), historyLimit);

        String originalText = req.getText();
        // 감정 분석
        String emotion = emotionClient.analyze(originalText);

        // 사용자 메시지 저장
        saveMessage(session, ChatMessage.Role.USER, originalText, emotion);

        // [핵심 1] 검색 로직 (PromptBuilder에게 판단 위임)
        List<SearchResDto> searchResults = null;
        if (promptBuilder.isSearchNeeded(originalText)) {
            System.out.println("🔎 [TEXT] 검색 키워드 감지 (by PromptBuilder): " + originalText);
            searchResults = naverSearchClient.search(originalText);
        }

        String contextualUserMsg = String.format("사용자 (감정: %s): %s", emotion, originalText);

        // [핵심 2] 프롬프트 빌드 (검색 결과 + 지역 코드 포함)
        List<MessageDto> prompt = promptBuilder.build(
                history,
                contextualUserMsg,
                emotion,
                session.getRegionCode(),
                seniorFriendly,
                searchResults
        );

        // LLM 답변 생성
        String reply = llmClient.chat(prompt, seniorFriendly);

        // 제목 생성 및 봇 메시지 저장
        generateTitleIfNeeded(session, originalText, reply);
        saveMessage(session, ChatMessage.Role.ASSISTANT, reply, null);

        // [핵심 3] TTS 음성 변환
        String replyAudioUrl = ttsClient.synthesize(reply, session.getRegionCode());
        List<MessageDto> updated = latestHistory(session.getId(), historyLimit);

        return ChatTextResponse.builder()
                .sessionId(session.getId())
                .userId(userId)
                .title(session.getTitle())
                .history(updated)
                .replyAudioUrl(replyAudioUrl)
                .build();
    }

    // --- 2. 음성 입력 처리 ---
    @Transactional
    public ChatVoiceResponse handleVoice(Long userId, String regionCode, MultipartFile file, Long sessionId) {
        ChatSession session = upsertSession(userId, sessionId, regionCode);

        // STT (음성 -> 텍스트)
        String asrText = asrClient.transcribe(session.getRegionCode(), file);
        String emotion = emotionClient.analyze(asrText);

        saveMessage(session, ChatMessage.Role.USER, asrText, emotion);

        List<MessageDto> history = latestHistory(session.getId(), historyLimit);

        // [핵심 1] 검색 로직 (음성 입력에 대해서도 수행)
        List<SearchResDto> searchResults = null;
        if (promptBuilder.isSearchNeeded(asrText)) {
            System.out.println("🔎 [VOICE] 검색 키워드 감지 (by PromptBuilder): " + asrText);
            searchResults = naverSearchClient.search(asrText);
        }

        String contextualUserMsg = String.format("사용자 (감정: %s): %s", emotion, asrText);

        // [핵심 2] 프롬프트 빌드
        List<MessageDto> prompt = promptBuilder.build(
                history,
                contextualUserMsg,
                emotion,
                session.getRegionCode(),
                seniorFriendly,
                searchResults
        );

        String reply = llmClient.chat(prompt, seniorFriendly);

        generateTitleIfNeeded(session, asrText, reply);
        saveMessage(session, ChatMessage.Role.ASSISTANT, reply, null);

        // [핵심 3] TTS 음성 변환
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

    // --- 3. 유틸리티 및 CRUD 메서드 ---

    @Transactional(readOnly = true)
    public List<MessageDto> getHistory(Long userId, Long sessionId) {
        ChatSession s = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("세션 없음"));
        if (!Objects.equals(s.getUserId(), userId)) {
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
                throw new SecurityException("권한 없음");
            }
            // 기존 세션이라도 지역 코드가 새로 들어오면 업데이트
            if (regionCode != null && !regionCode.isBlank()) {
                session.setRegionCode(regionCode);
            }
        } else {
            session = new ChatSession();
            session.setUserId(userId);
            // 새 세션 생성 시 기본값 설정
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