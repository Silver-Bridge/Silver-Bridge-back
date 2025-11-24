package com.silverbridge.backend.service.chatbot;

import com.silverbridge.backend.dto.chatbot.MessageDto;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// LLM에 전달할 최종 프롬프트를 구성하는 빌더
@Component
public class PromptBuilder {

    // 대화 기록, 사용자 메시지, 시스템 메시지를 조합하여 LLM용 프롬프트 생성
    public List<MessageDto> build(List<MessageDto> history, String userMsg, boolean seniorFriendly) {
        List<MessageDto> msgs = new ArrayList<>();

        // [수정] 노인 친화 모드 시스템 프롬프트 강화 (답변 길이/어휘 가이드 포함)
        String system = seniorFriendly
                ? "당신은 70대 어르신을 위한 전문적이고 다정다감한 인공지능 말벗입니다. " +
                "사용자가 '감정: [XX]' 상태로 말한 내용을 분석하고 공감하여, " +
                "최대한 쉬운 어휘와 짧고 친절한 문장으로 답변해야 합니다. " +
                "답변은 50자 내외로 간결하게 작성합니다. 필요 없는 긴 설명은 생략합니다."
                : "You are a helpful assistant.";

        // 1. 시스템 메시지 추가
        msgs.add(new MessageDto("system", system));

        // 2. 기존 대화 기록 추가 (시스템 프롬프트는 emotion 필드 제외)
        if (history != null && !history.isEmpty()) {
            msgs.addAll(history.stream()
                    .map(m -> new MessageDto(m.getRole(), m.getContent()))
                    .collect(Collectors.toList()));
        }

        // 3. 현재 사용자 메시지 추가 (ChatService에서 감정 정보가 이미 content에 포함되어 있음)
        msgs.add(new MessageDto("user", userMsg));

        return msgs;
    }
    public List<MessageDto> buildTitlePrompt(String userMsg, String botResponse) {
        List<MessageDto> msgs = new ArrayList<>();

        String systemInstruction = "대화 내용을 요약하여 15자 이내의 간결한 제목을 짓는 전문가입니다. " +
                "따옴표나 문장 부호 없이 명사형으로 끝내고, 제목 텍스트만 출력하세요.";
        msgs.add(new MessageDto("system", systemInstruction));

        String content = "다음 대화를 보고 제목을 지어줘:\n" +
                "사용자: " + userMsg + "\n" +
                "AI: " + botResponse;
        msgs.add(new MessageDto("user", content));

        return msgs;
    }
}