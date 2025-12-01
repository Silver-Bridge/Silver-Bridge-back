package com.silverbridge.backend.service.chatbot;

import com.silverbridge.backend.dto.chatbot.MessageDto;
import com.silverbridge.backend.dto.chatbot.ScheduleCommandDto; // DTO import 필수
import com.fasterxml.jackson.databind.ObjectMapper; // JSON 파싱용
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class LlmClient {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper; // JSON 변환을 위해 주입

    // [기존 chat 메서드는 그대로 유지...]
    public String chat(List<MessageDto> messages, boolean seniorFriendly) {
        // ... (아까 작성한 코드 그대로 두세요) ...
        // (생략: 공간 절약을 위해)
        // 여기에 기존 chat 로직이 있어야 합니다!

        // --- 복붙용 기존 chat 코드 ---
        List<Message> springAiMessages = messages.stream()
                .map(dto -> {
                    switch (dto.getRole().toLowerCase()) {
                        case "system": return new SystemMessage(dto.getContent());
                        case "user": return new UserMessage(dto.getContent());
                        case "assistant": return new AssistantMessage(dto.getContent());
                        default: return new UserMessage(dto.getContent());
                    }
                })
                .collect(Collectors.toList());
        try {
            OpenAiChatOptions options = OpenAiChatOptions.builder()
                    .withMaxTokens(2000)
                    .withTemperature(0.7F)
                    .build();
            Prompt prompt = new Prompt(springAiMessages, options);
            ChatResponse response = chatClient.prompt(prompt).call().chatResponse();
            if (response != null && response.getResult() != null) {
                return response.getResult().getOutput().getContent();
            } else {
                return "LLM 응답이 비어있습니다.";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "죄송합니다. 오류가 발생했습니다.";
        }
        // -------------------------
    }

    /**
     * [업그레이드된 메서드] 사용자 의도(Intent) 및 데이터 추출
     * - 일정 추가/조회/삭제 및 알림 설정까지 분석
     */
    public ScheduleCommandDto extractCommand(String userText) {
        String systemPrompt = """
            You are an Intent Classification & Data Extraction AI.
            Current Time: %s
            
            Analyze the user's input and extract the intent into JSON format.
            
            Actions:
            - ADD: Add a new schedule (e.g., "내일 2시에 약속 잡아줘")
            - CHECK: Check schedules (e.g., "내일 일정 있어?", "이번주 스케줄 알려줘")
            - DELETE: Delete a schedule (e.g., "치과 예약 취소해", "3시 일정 지워줘")
            - ALARM: Toggle alarm settings (e.g., "알림 켜줘", "알람 꺼", "방해금지")
            - NONE: General conversation (e.g., "안녕", "심심해")
            
            JSON Fields:
            - action: "ADD", "CHECK", "DELETE", "ALARM", or "NONE"
            - title: (For ADD/DELETE) Summary of the event.
            - startDateTime: (For ADD) 'YYYY-MM-DDTHH:mm:ss'
            - targetDate: (For CHECK) 'YYYY-MM-DD' (Target date to check)
            - alarmOn: (For ALARM) true for ON, false for OFF
            
            Rules:
            1. Calculate relative dates based on Current Time. Default time is 09:00:00.
            2. If checking for "tomorrow", targetDate is tomorrow's date.
            3. Output ONLY the raw JSON.
            
            Examples:
            1. "내일 점심 약속 잡아줘" -> {"action": "ADD", "title": "점심 약속", "startDateTime": "2025-01-02T12:00:00"}
            2. "내일 일정 뭐 있어?" -> {"action": "CHECK", "targetDate": "2025-01-02"}
            3. "알림 좀 꺼줘" -> {"action": "ALARM", "alarmOn": false}
            """;

        String now = java.time.LocalDateTime.now().toString();
        String finalSystemPrompt = String.format(systemPrompt, now);

        try {
            OpenAiChatOptions options = OpenAiChatOptions.builder()
                    .withTemperature(0.0F) // 정확도 중요
                    .build();

            Prompt prompt = new Prompt(List.of(
                    new SystemMessage(finalSystemPrompt),
                    new UserMessage(userText)
            ), options);

            ChatResponse response = chatClient.prompt(prompt).call().chatResponse();
            String jsonOutput = response.getResult().getOutput().getContent()
                    .replace("```json", "").replace("```", "").trim();

            // JSON 문자열을 DTO 객체로 변환
            return objectMapper.readValue(jsonOutput, ScheduleCommandDto.class);

        } catch (Exception e) {
            System.err.println("명령 추출 실패: " + e.getMessage());
            // 실패 시 아무것도 안 함(NONE)으로 처리
            return ScheduleCommandDto.builder().action(ScheduleCommandDto.Action.NONE).build();
        }
    }
}