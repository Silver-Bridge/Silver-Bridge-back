package com.silverbridge.backend.service.chatbot;

import com.silverbridge.backend.dto.chatbot.MessageDto;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// LLM에 전달할 최종 프롬프트를 구성하는 빌더
@Component
public class PromptBuilder {

    /**
     * 감정 코드(0~6)를 반영하여 시스템 프롬프트를 동적으로 생성하는 메서드
     */
    public List<MessageDto> build(List<MessageDto> history, String userMsg, String emotionCode, boolean seniorFriendly) {
        List<MessageDto> msgs = new ArrayList<>();

        // 1. 기본 시스템 페르소나 설정 (영어로 번역 및 최적화)
        StringBuilder systemPrompt = new StringBuilder();

        if (seniorFriendly) {
            // ▼▼▼ [Senior Friendly English Prompt] ▼▼▼
            systemPrompt.append("You are 'SilverBridge', a professional and warm-hearted AI companion for seniors. ");
            systemPrompt.append("Always respond in Korean. Use simple words and short, kind sentences. ");
            systemPrompt.append("Keep the response concise, under 50 characters, and omit lengthy explanations. ");

            // ▼▼▼ [핵심] 감정 코드에 따른 맞춤형 행동 지침 추가 (영어로) ▼▼▼
            String instruction = getEmotionInstruction(emotionCode);
            systemPrompt.append("\n\n[Current User State and Response Directive]\n").append(instruction);
        } else {
            systemPrompt.append("You are a helpful assistant.");
        }

        // 2. 시스템 메시지 추가
        msgs.add(new MessageDto("system", systemPrompt.toString()));

        // 3. 기존 대화 기록 추가
        if (history != null && !history.isEmpty()) {
            msgs.addAll(history.stream()
                    .map(m -> new MessageDto(m.getRole(), m.getContent()))
                    .collect(Collectors.toList()));
        }

        // 4. 현재 사용자 메시지 추가
        msgs.add(new MessageDto("user", userMsg));

        return msgs;
    }

    public List<MessageDto> buildTitlePrompt(String userMsg, String botResponse) {
        List<MessageDto> msgs = new ArrayList<>();

        // ▼▼▼ [Title Generation English Prompt] ▼▼▼
        String systemInstruction = "You are an expert in summarizing conversations into a concise title under 15 characters. " +
                "Output only the title text in Korean (한국어), ending in a noun form, without quotes or punctuation.";
        msgs.add(new MessageDto("system", systemInstruction));

        String content = "Generate a title for the following conversation:\n" +
                "User: " + userMsg + "\n" +
                "AI: " + botResponse;
        msgs.add(new MessageDto("user", content));

        return msgs;
    }

    /**
     * 감정 코드(0~6)별 AI 행동 지침 매핑 및 반환 (사용자 정의 분류표 및 영어 적용)
     */
    private String getEmotionInstruction(String code) {
        if (code == null) code = "6"; // 기본값 (Neutral)

        return switch (code) {
            case "0" -> // 긍정 계열 (기쁨, 행복, 사랑스러움)
                    "User is highly positive/happy. Share their joy, actively affirm them, and amplify the cheerful atmosphere.";

            case "1" -> // 슬픔 계열 (슬픔, 상처)
                    "User is sad/hurt. Offer deep empathy and gentle consolation, focusing on active listening and warmth. Acknowledge their pain first.";

            case "2" -> // 분노 계열 (분노, 화남)
                    "User is angry. Do not contradict or defend. Acknowledge their anger immediately, then focus on calming them with a very composed tone.";

            case "3" -> // 불안 계열 (불안, 공포, 두려움, 당황)
                    "User is anxious/fearful. Provide reassurance and stability with a firm, trustworthy tone (e.g., 'Don't worry, I will help').";

            case "4" -> // 놀람 계열 (놀람)
                    "User is surprised/shocked. Use a careful, calm tone. Prioritize understanding the situation and helping the user regain composure.";

            case "5" -> // 혐오 계열 (혐오)
                    "User is expressing disgust/discomfort. Immediately pivot away from the topic to a cheerful, positive subject to improve their mood.";

            case "6" -> // 중립 계열 (중립)
                    "User is neutral. Introduce friendly, useful daily topics (health, weather, meals) to initiate and maintain an engaging conversation.";

            default ->
                    "Emotion code is unclear. Respond kindly and warmly.";
        };
    }
}