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

    // 어르신 친화적 답변 모드 활성화 여부
    @Value("${chatbot.senior-friendly:true}")
    private boolean seniorFriendly;

    // LLM에 전달할 대화 기록 최대 개수
    @Value("${chatbot.history-limit:20}")
    private int historyLimit;

    // 텍스트 입력을 받아 챗봇 응답을 생성하는 전체 과정 처리
    @Transactional
    public ChatTextResponse handleText(Long userId, ChatTextRequest req) {
        // 세션 조회 또는 신규 생성
        ChatSession session = upsertSession(userId, req.getSessionId(), req.getRegionCode());
        // LLM에 전달할 최근 대화 기록 조회
        List<MessageDto> history = latestHistory(session.getId(), historyLimit);
        // 사용자 메시지 DB 저장
        saveMessage(session, ChatMessage.Role.USER, req.getText());
        // LLM에 전달할 프롬프트 조합
        List<MessageDto> prompt = promptBuilder.build(history, req.getText(), seniorFriendly);
        // LLM 호출하여 답변 생성
        String reply = llmClient.chat(prompt, seniorFriendly);
        // 챗봇 답변 DB 저장
        saveMessage(session, ChatMessage.Role.ASSISTANT, reply);
        // 최종 응답 데이터 구성 및 반환
        List<MessageDto> updated = latestHistory(session.getId(), historyLimit);
        return ChatTextResponse.builder()
                .sessionId(session.getId())
                .replyText(reply)
                .history(updated)
                .build();
    }

    // 음성 입력을 받아 텍스트로 변환 후 챗봇 응답 생성
    @Transactional
    public ChatVoiceResponse handleVoice(Long userId, String regionCode, MultipartFile file) {
        // 음성 입력용 신규 세션 생성
        ChatSession session = upsertSession(userId, null, regionCode);
        // 음성을 텍스트로 변환 (ASR)
        String asrText = asrClient.transcribe(session.getRegionCode(), file);
        // 변환된 사용자 메시지 DB 저장
        saveMessage(session, ChatMessage.Role.USER, asrText);
        // LLM에 전달할 최근 대화 기록 조회 및 프롬프트 조합
        List<MessageDto> history = latestHistory(session.getId(), historyLimit);
        List<MessageDto> prompt = promptBuilder.build(history, asrText, seniorFriendly);
        // LLM 호출하여 답변 생성
        String reply = llmClient.chat(prompt, seniorFriendly);
        // 챗봇 답변 DB 저장
        saveMessage(session, ChatMessage.Role.ASSISTANT, reply);
        // 최종 응답 데이터 구성 및 반환
        return ChatVoiceResponse.builder()
                .sessionId(session.getId())
                .asrText(asrText)
                .replyText(reply)
                .build();
    }

    // 특정 세션의 대화 기록 조회
    @Transactional(readOnly = true)
    public List<MessageDto> getHistory(Long userId, Long sessionId) {
        // 세션 조회 및 소유권 확인
        ChatSession s = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("세션 없음"));
        if (!Objects.equals(s.getUserId(), userId)) {
            throw new AccessControlException("권한 없음");
        }
        // 최신 대화 기록 반환
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
        // 세션 조회 및 소유권 확인
        ChatSession session = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("세션 없음"));
        if (!Objects.equals(session.getUserId(), userId)) {
            throw new AccessControlException("본인 세션만 삭제할 수 있습니다.");
        }
        // 연관된 메시지 삭제 후 세션 삭제
        messageRepo.deleteAll(messageRepo.findTop50BySessionIdOrderByCreatedAtDesc(sessionId));
        sessionRepo.delete(session);
    }

    // 기존 세션 조회 또는 신규 세션 생성 (Upsert)
    private ChatSession upsertSession(Long userId, Long sessionId, String regionCode) {
        ChatSession session;
        if (sessionId != null) {
            // 기존 세션 ID가 있는 경우, 조회 및 소유권 확인
            session = sessionRepo.findById(sessionId)
                    .orElseThrow(() -> new IllegalArgumentException("세션 없음"));
            if (!Objects.equals(session.getUserId(), userId)) {
                throw new AccessControlException("권한 없음");
            }
            // 지역 코드 변경 요청이 있으면 업데이트
            if (regionCode != null && !regionCode.isBlank()) {
                session.setRegionCode(regionCode);
            }
        } else {
            // 새 세션 생성
            session = new ChatSession();
            session.setUserId(userId);
            session.setRegionCode(regionCode == null || regionCode.isBlank() ? "std" : regionCode);
        }
        // 변경사항 저장 (save는 insert와 update 모두 처리)
        return sessionRepo.save(session);
    }

    // 채팅 메시지 DB 저장
    private void saveMessage(ChatSession s, ChatMessage.Role role, String content) {
        ChatMessage m = new ChatMessage();
        m.setSession(s);
        m.setRole(role);
        m.setContent(content);
        messageRepo.save(m);
    }

    // 세션의 최근 대화 기록을 DTO 리스트로 변환하여 조회
    private List<MessageDto> latestHistory(Long sessionId, int limit) {
        // DB에서 생성 시간 역순으로 조회 후, 다시 시간순으로 정렬
        return messageRepo.findTop50BySessionIdOrderByCreatedAtDesc(sessionId).stream()
                .sorted(Comparator.comparing(ChatMessage::getCreatedAt))
                .limit(limit)
                .map(m -> new MessageDto(m.getRole().name().toLowerCase(), m.getContent()))
                .collect(Collectors.toList());
    }
}