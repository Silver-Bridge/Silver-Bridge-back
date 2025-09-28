package com.silverbridge.backend.service.chatbot;

import com.silverbridge.backend.dto.chatbot.MessageDto;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class PromptBuilder {

    public List<MessageDto> build(List<MessageDto> history, String userMsg, boolean seniorFriendly) {
        List<MessageDto> msgs = new ArrayList<>();
        String system = seniorFriendly
                ? "당신은 노인 친화 한국어 챗봇입니다. 쉬운 어휘, 짧은 문장, 또박또박 설명하세요."
                : "You are a helpful assistant.";
        msgs.add(new MessageDto("system", system));
        if (history != null && !history.isEmpty()) msgs.addAll(history);
        msgs.add(new MessageDto("user", userMsg));
        return msgs;
    }
}
