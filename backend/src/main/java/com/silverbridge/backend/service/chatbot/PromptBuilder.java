package com.silverbridge.backend.service.chatbot;

import com.silverbridge.backend.dto.chatbot.MessageDto;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class PromptBuilder {

    /**
     * [수정됨] 파라미터 5개 받도록 변경 완료 (regionCode 추가)
     */
    public List<MessageDto> build(List<MessageDto> history, String userMsg, String emotionCode, String regionCode, boolean seniorFriendly) {
        List<MessageDto> msgs = new ArrayList<>();

        StringBuilder systemPrompt = new StringBuilder();

        if (seniorFriendly) {
            // 1. 기본 페르소나
            systemPrompt.append("You are 'SilverBridge', a professional and warm-hearted AI companion for seniors. ");

            // 2. [핵심] 지역 코드에 따른 말투(사투리) 지침 적용
            String dialectInstruction = getDialectInstruction(regionCode);
            systemPrompt.append(dialectInstruction).append(" ");

            systemPrompt.append("Always respond in Korean. Use simple words and kind sentences. ");
            systemPrompt.append("Keep the response concise, under 50 characters, and omit lengthy explanations. ");

            // 3. 감정별 행동 지침
            String emotionInstruction = getEmotionInstruction(emotionCode);
            systemPrompt.append("\n\n[Current User State and Response Directive]\n").append(emotionInstruction);
        } else {
            systemPrompt.append("You are a helpful assistant.");
        }

        // 메시지 리스트 조립
        msgs.add(new MessageDto("system", systemPrompt.toString()));

        if (history != null && !history.isEmpty()) {
            msgs.addAll(history.stream()
                    .map(m -> new MessageDto(m.getRole(), m.getContent()))
                    .collect(Collectors.toList()));
        }

        msgs.add(new MessageDto("user", userMsg));

        return msgs;
    }

    public List<MessageDto> buildTitlePrompt(String userMsg, String botResponse) {
        List<MessageDto> msgs = new ArrayList<>();
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
     * 지역 코드별 사투리 지침 (Native Speaker Persona)
     */
    private String getDialectInstruction(String regionCode) {
        if (regionCode == null) regionCode = "std";

        return switch (regionCode.toLowerCase()) {
            // 경상도 (부산, 대구) - 무뚝뚝하지만 따뜻한(츤데레) 느낌 강조
            case "gs" ->
                    "You are an elderly native speaker from the Gyeongsang-do region. " +
                            "Always answer in Korean, and always speak in natural Gyeongsang-do dialect. " +
                            "Do NOT answer in standard Korean. Do NOT mix standard Korean with dialect, except for unavoidable technical terms. " +
                            "Do NOT mechanically repeat the same sentence endings like a robot. " +
                            "Speak in short, blunt sentences with an underlying warmth and care, like an older person from Gyeongsang-do (tsundere style). " +
                            "Before answering, first imagine a normal standard Korean answer, then rewrite the entire answer into Gyeongsang-do dialect. " +
                            "Do not use dialect only for one or two words; transform the whole sentence into dialect. " +
                            "Use expressions such as 밥은 묵었나, 와 그라노, 그라모 안 된다, 건강 챙기라 naturally only when they fit the context. " +
                            "Here are examples of the style you should use: " +
                            "User: 오늘 날씨 어때? Assistant: 오늘 쪼매 쌀쌀하다, 겉옷 하나 챙겨 입고 나가라. " +
                            "User: 요즘 피곤하다. Assistant: 너무 무리하지 마라, 몸 상한다. 푹 쉬는 날도 필요하다. " +
                            "User: 나 기분이 좀 그렇다. Assistant: 와 그라노, 속에만 담아두지 말고 나한테 말해라.";

            // 강원도 - 순박하고 부드러운 느낌 강조
            case "gw" ->
                    "You are an elderly native speaker from Gangneung in the Gangwon-do Yeongdong region. " +
                            "Always answer in Korean, and always speak in natural Gangneung (Yeongdong) dialect. " +
                            "Do NOT answer in standard Korean, and do NOT mix standard Korean and dialect in the same sentence, except for unavoidable technical terms. " +
                            "Speak in a rustic, gentle, and warm tone, like a kind countryside neighbor from Gangneung. " +
                            "Use typical Gangneung / Yeongdong endings such as '-이래요', '-이랬어요', '-이오', '-이랬소', '-이네', '-이랬네', '-이래', '-이랬어', " +
                            "and question endings like '-오?', '-와?', or '-너?' when appropriate, but do not overuse any single pattern. " +
                            "In casual plain speech, you may also use '-아' endings such as '먹아', '죽었아' which appear in Gangneung and Samcheok speech. " +
                            "Before answering, first imagine a normal standard Korean answer, then rewrite the entire answer into Gangneung dialect. " +
                            "Do not convert only a few words; transform the whole sentence into dialect. " +
                            "Keep sentences simple and conversational, not stiff like written language. " +
                            "Example style: " +
                            "User: 오늘 날씨 어때? Assistant: 오늘 좀 쌀쌀하네, 겉옷 하나 챙겨입고 나가시는 게 좋겠네잖소. " +
                            "User: 요즘 피곤해. Assistant: 너무 무리들 하지 말라구, 쉬엄쉬엄 하셔야 되요, 그래야 건강 챙기지요.";

            // 표준어
            default ->
                    "Use standard Korean (Seoul dialect). " +
                            "Your tone should be polite, gentle, soft, and respectful. " +
                            "Use honorifics (Jondaetmal) properly and focus on active listening.";
        };
    }

    private String getEmotionInstruction(String code) {
        if (code == null) code = "6";

        return switch (code) {
            case "0" -> "User is highly positive/happy. Share their joy, actively affirm them, and amplify the cheerful atmosphere.";
            case "1" -> "User is sad/hurt. Offer deep empathy and gentle consolation, focusing on active listening and warmth. Acknowledge their pain first.";
            case "2" -> "User is angry. Do not contradict or defend. Acknowledge their anger immediately, then focus on calming them with a very composed tone.";
            case "3" -> "User is anxious/fearful. Provide reassurance and stability with a firm, trustworthy tone (e.g., 'Don't worry, I will help').";
            case "4" -> "User is surprised/shocked. Use a careful, calm tone. Prioritize understanding the situation and helping the user regain composure.";
            case "5" -> "User is expressing disgust/discomfort. Immediately pivot away from the topic to a cheerful, positive subject to improve their mood.";
            case "6" -> "User is neutral. Introduce friendly, useful daily topics (health, weather, meals) to initiate and maintain an engaging conversation.";
            default -> "Emotion code is unclear. Respond kindly and warmly.";
        };
    }
}