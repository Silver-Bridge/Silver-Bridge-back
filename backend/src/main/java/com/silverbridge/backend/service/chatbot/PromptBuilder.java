package com.silverbridge.backend.service.chatbot;

import com.silverbridge.backend.dto.chatbot.MessageDto;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

// LLM에 전달할 최종 프롬프트를 구성하는 빌더
@Component
public class PromptBuilder {

    // 대화 기록, 사용자 메시지, 시스템 메시지를 조합하여 LLM용 프롬프트 생성
    public List<MessageDto> build(List<MessageDto> history, String userMsg, boolean seniorFriendly) {
        List<MessageDto> msgs = new ArrayList<>();

        // 노인 친화 모드 여부에 따라 시스템 메시지 설정
        String system = seniorFriendly
                ? "당신은 노인 친화 한국어 챗봇입니다. 쉬운 어휘, 짧은 문장, 또박또박 설명하세요."
                : "You are a helpful assistant.";

        // 1. 시스템 메시지 추가
        msgs.add(new MessageDto("system", system));
        // 2. 기존 대화 기록 추가
        if (history != null && !history.isEmpty()) msgs.addAll(history);
        // 3. 현재 사용자 메시지 추가
        msgs.add(new MessageDto("user", userMsg));

        return msgs;
    }
}