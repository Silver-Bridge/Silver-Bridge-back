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

    @Value("${chatbot.senior-friendly:true}")
    private boolean seniorFriendly;

    @Value("${chatbot.history-limit:20}")
    private int historyLimit;

    /**
     * 텍스트 입력 처리
     */
    @Transactional
    public ChatTextResponse handleText(Long userId, ChatTextRequest req) {
        ChatSession session = upsertSession(userId, req.getSessionId(), req.getRegionCode());

        List<MessageDto> history = latestHistory(session.getId(), historyLimit);

        saveMessage(session, ChatMessage.Role.USER, req.getText());

        List<MessageDto> prompt = promptBuilder.build(history, req.getText(), seniorFriendly);
        String reply = llmClient.chat(prompt, seniorFriendly);

        saveMessage(session, ChatMessage.Role.ASSISTANT, reply);

        List<MessageDto> updated = latestHistory(session.getId(), historyLimit);
        return ChatTextResponse.builder()
                .sessionId(session.getId())
                .replyText(reply)
                .history(updated)
                .build();
    }

    /**
     * 음성 입력 처리
     */
    @Transactional
    public ChatVoiceResponse handleVoice(Long userId, String regionCode, MultipartFile file) {
        ChatSession session = upsertSession(userId, null, regionCode);

        String asrText = asrClient.transcribe(session.getRegionCode(), file);

        saveMessage(session, ChatMessage.Role.USER, asrText);

        List<MessageDto> history = latestHistory(session.getId(), historyLimit);
        List<MessageDto> prompt = promptBuilder.build(history, asrText, seniorFriendly);
        String reply = llmClient.chat(prompt, seniorFriendly);

        saveMessage(session, ChatMessage.Role.ASSISTANT, reply);

        return ChatVoiceResponse.builder()
                .sessionId(session.getId())
                .asrText(asrText)
                .replyText(reply)
                .build();
    }

    /**
     * 히스토리 조회
     */
    @Transactional(readOnly = true)
    public List<MessageDto> getHistory(Long userId, Long sessionId) {
        ChatSession s = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("세션 없음"));
        if (!Objects.equals(s.getUserId(), userId)) {
            throw new AccessControlException("권한 없음");
        }
        return latestHistory(sessionId, Math.max(historyLimit, 50));
    }

    /**
     * ✅ 사용자 세션 목록 조회
     */
    @Transactional(readOnly = true)
    public List<ChatSession> getSessions(Long userId) {
        return sessionRepo.findByUserIdOrderByCreatedAtDesc(userId);
    }

    /**
     * ✅ 사용자 세션 삭제
     */
    @Transactional
    public void deleteSession(Long userId, Long sessionId) {
        ChatSession session = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("세션 없음"));

        if (!Objects.equals(session.getUserId(), userId)) {
            throw new AccessControlException("본인 세션만 삭제할 수 있습니다.");
        }

        // 메시지도 같이 삭제
        messageRepo.deleteAll(messageRepo.findTop50BySessionIdOrderByCreatedAtDesc(sessionId));
        sessionRepo.delete(session);
    }

    /**
     * 세션 생성 or 갱신
     */
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

    /**
     * 메시지 저장
     */
    private void saveMessage(ChatSession s, ChatMessage.Role role, String content) {
        ChatMessage m = new ChatMessage();
        m.setSession(s);
        m.setRole(role);
        m.setContent(content);
        messageRepo.save(m);
    }

    /**
     * 세션 최신 대화 불러오기
     */
    private List<MessageDto> latestHistory(Long sessionId, int limit) {
        return messageRepo.findTop50BySessionIdOrderByCreatedAtDesc(sessionId).stream()
                .sorted(Comparator.comparing(ChatMessage::getCreatedAt))
                .limit(limit)
                .map(m -> new MessageDto(m.getRole().name().toLowerCase(), m.getContent()))
                .collect(Collectors.toList());
    }
}
