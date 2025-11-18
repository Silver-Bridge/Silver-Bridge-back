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

// 챗봇 기능의 핵심 비즈니스 로직을 처리하는 서비스
@Service
@RequiredArgsConstructor
public class ChatService {

    // 의존성 주입
    private final ChatSessionRepository sessionRepo;
    private final ChatMessageRepository messageRepo;
    private final AsrClient asrClient;
    private final LlmClient llmClient;
    private final PromptBuilder promptBuilder;
    private final EmotionClient emotionClient;

    // [추가] TTS 더미 클라이언트 주입
    private final TtsClient ttsClient;

    // 어르신 친화적 답변 모드 활성화 여부
    @Value("${chatbot.senior-friendly:true}")
    private boolean seniorFriendly;

    // LLM에 전달할 대화 기록 최대 개수
    @Value("${chatbot.history-limit:20}")
    private int historyLimit;

    // 텍스트 입력을 받아 챗봇 응답을 생성하는 전체 과정 처리
    @Transactional
    public ChatTextResponse handleText(Long userId, ChatTextRequest req) {
        ChatSession session = upsertSession(userId, req.getSessionId(), req.getRegionCode());
        List<MessageDto> history = latestHistory(session.getId(), historyLimit);

        String originalText = req.getText();
        String emotion = emotionClient.analyze(originalText);

        // [수정 후] 감정 정보 포함하여 DB 저장
        saveMessage(session, ChatMessage.Role.USER, originalText, emotion);

        String contextualUserMsg = String.format("사용자 (감정: %s): %s", emotion, originalText);

        List<MessageDto> prompt = promptBuilder.build(history, contextualUserMsg, seniorFriendly);
        String reply = llmClient.chat(prompt, seniorFriendly);

        // [수정 후] 챗봇 응답 저장 (챗봇 감정은 null)
        saveMessage(session, ChatMessage.Role.ASSISTANT, reply, null);

        // [추가] TTS 음성 변환 (더미 URL 생성)
        String replyAudioUrl = ttsClient.synthesize(reply, session.getRegionCode());

        // [수정] DB에 저장된 감정까지 포함하여 최신 기록 다시 조회
        List<MessageDto> updated = latestHistory(session.getId(), historyLimit);

        return ChatTextResponse.builder()
                .sessionId(session.getId())
                // [삭제] 중복 필드 주석 처리
                // .replyText(reply)
                .history(updated)
                // [삭제] 중복 필드 주석 처리
                // .emotion(emotion)
                // [추가] 음성 URL 포함
                .replyAudioUrl(replyAudioUrl)
                .build();
    }

    // 음성 입력을 받아 텍스트로 변환 후 챗봇 응답 생성
    @Transactional
    public ChatVoiceResponse handleVoice(Long userId, String regionCode, MultipartFile file, Long sessionId) {
        // sessionId 전달하여 세션 유지
        ChatSession session = upsertSession(userId, sessionId, regionCode);

        String asrText = asrClient.transcribe(session.getRegionCode(), file);
        String emotion = emotionClient.analyze(asrText);

        // 변환된 사용자 메시지 및 감정 정보 DB 저장
        saveMessage(session, ChatMessage.Role.USER, asrText, emotion);

        String contextualUserMsg = String.format("사용자 (감정: %s): %s", emotion, asrText);

        List<MessageDto> history = latestHistory(session.getId(), historyLimit);
        List<MessageDto> prompt = promptBuilder.build(history, contextualUserMsg, seniorFriendly);

        String reply = llmClient.chat(prompt, seniorFriendly);

        // 챗봇 응답 저장 (감정 null)
        saveMessage(session, ChatMessage.Role.ASSISTANT, reply, null);

        // TTS 음성 변환
        String replyAudioUrl = ttsClient.synthesize(reply, session.getRegionCode());

        // DB에 저장된 감정까지 포함하여 최신 기록 다시 조회
        List<MessageDto> updatedHistory = latestHistory(session.getId(), historyLimit);

        return ChatVoiceResponse.builder()
                .sessionId(session.getId())
                // [삭제] 중복 필드 주석 처리
                // .asrText(asrText)
                // .replyText(reply)
                // .emotion(emotion)
                .history(updatedHistory)
                // 음성 URL 포함
                .replyAudioUrl(replyAudioUrl)
                .build();
    }

    // 특정 세션의 대화 기록 조회
    @Transactional(readOnly = true)
    public List<MessageDto> getHistory(Long userId, Long sessionId) {
        ChatSession s = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("세션 없음"));
        if (!Objects.equals(s.getUserId(), userId)) {
            throw new AccessControlException("권한 없음");
        }
        return latestHistory(sessionId, Math.max(historyLimit, 50));
    }

    // 사용자 본인의 전체 세션 목록 조회
    @Transactional(readOnly = true)
    public List<ChatSession> getSessions(Long userId) {
        return sessionRepo.findByUserIdOrderByCreatedAtDesc(userId);
    }

    // 사용자 세션 삭제
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

    // 기존 세션 조회 또는 신규 세션 생성
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
            session.setRegionCode(regionCode == null || regionCode.isBlank() ? "std" : regionCode);
        }
        return sessionRepo.save(session);
    }


    // emotion 파라미터 추가하여 DB 저장
    private void saveMessage(ChatSession s, ChatMessage.Role role, String content, String emotion) {
        ChatMessage m = new ChatMessage();
        m.setSession(s);
        m.setRole(role);
        m.setContent(content);
        m.setEmotion(emotion); // 엔티티에 감정 저장
        messageRepo.save(m);
    }

    // 세션의 최근 대화 기록을 DTO 리스트로 변환하여 조회
    private List<MessageDto> latestHistory(Long sessionId, int limit) {
        return messageRepo.findTop50BySessionIdOrderByCreatedAtDesc(sessionId).stream()
                .sorted(Comparator.comparing(ChatMessage::getCreatedAt))
                .limit(limit)
                // [수정 전] 감정 미포함 생성자 사용
                // .map(m -> new MessageDto(m.getRole().name().toLowerCase(), m.getContent()))
                // [수정 후] DB에 저장된 감정(m.getEmotion())을 포함하여 DTO 생성
                .map(m -> new MessageDto(
                        m.getRole().name().toLowerCase(),
                        m.getContent(),
                        m.getEmotion()
                ))
                .collect(Collectors.toList());
    }
}