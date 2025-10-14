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
        // 음성 입력은 항상 새 세션으로 처리 (기존 세션 ID는 null)
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

    // 특정 세션의 대화 기록 조회 및 소유권 검증
    @Transactional(readOnly = true)