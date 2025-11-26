package com.silverbridge.backend.service.chatbot;

import com.silverbridge.backend.dto.chatbot.MessageDto;
import com.silverbridge.backend.dto.chatbot.SearchResDto;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class PromptBuilder {

    private static final String[] SEARCH_KEYWORDS = {
            "복지", "혜택", "지원금", "정책", "센터",
            "추천", "어디", "찾아줘", "알려줘", "병원", "약국",
            "뉴스", "정보", "어떻게"
    };

    public boolean isSearchNeeded(String userMsg) {
        if (userMsg == null || userMsg.isBlank()) return false;
        for (String keyword : SEARCH_KEYWORDS) {
            if (userMsg.contains(keyword)) return true;
        }
        return false;
    }

    public List<MessageDto> build(List<MessageDto> history, String userMsg, String emotionCode, String regionCode, boolean seniorFriendly, List<SearchResDto> searchResults) {
        List<MessageDto> msgs = new ArrayList<>();
        StringBuilder systemPrompt = new StringBuilder();

        boolean hasSearchInfo = (searchResults != null && !searchResults.isEmpty());

        if (seniorFriendly) {
            // 1. 기본 역할 정의
            systemPrompt.append("You are 'SilverBridge', a professional AI companion for seniors. ");

            // 2. 지역별 페르소나 (말투/성격) 설정 - 가장 우선순위 높음
            systemPrompt.append(getDialectInstruction(regionCode)).append("\n");

            // 3. 검색 결과(RAG) 주입
            if (hasSearchInfo) {
                systemPrompt.append("\n### [Reference Information] ###\n");
                systemPrompt.append("You MUST answer based on the search results below.\n");
                systemPrompt.append("1. Extract specific program names, locations, or benefits.\n");
                systemPrompt.append("2. Do NOT generalize. Mention specific names found in the results.\n");
                systemPrompt.append("3. Explain 2-3 key items clearly.\n");
                // [중요] 정보는 정확하게, 말투는 페르소나 유지
                systemPrompt.append("4. IMPORTANT: Convert the explanation into the defined dialect/tone above, but keep the proper nouns (names) accurate.\n");

                for (int i = 0; i < searchResults.size(); i++) {
                    SearchResDto item = searchResults.get(i);
                    systemPrompt.append(String.format("- %s : %s\n", item.getTitle(), item.getDescription()));
                }
                systemPrompt.append("### End of Reference ###\n");
            }

            // 4. 대화 가이드라인
            systemPrompt.append("\n[Response Guidelines]\n");
            systemPrompt.append("- Always respond in Korean.\n");

            // 공감/반응 지시
            systemPrompt.append("- Add a brief empathetic phrase (e.g., '그럴 수 있어요', '좋은 생각이에요') if appropriate.\n");

            // 길이 조정
            if (hasSearchInfo) {
                systemPrompt.append("- Since you are explaining information, you can write up to 400 characters.\n");
                systemPrompt.append("- Make sure the user clearly understands the specific benefits.\n");
            } else {
                systemPrompt.append("- Keep it short for casual talk (around 100~150 characters).\n");
                systemPrompt.append("- Avoid long lectures.\n");
            }

            // 5. 감정 지침
            systemPrompt.append("\n[User Emotion: ").append(getEmotionInstruction(emotionCode)).append("]\n");

        } else {
            systemPrompt.append("You are a helpful assistant.");
        }

        msgs.add(new MessageDto("system", systemPrompt.toString()));

        if (history != null && !history.isEmpty()) {
            msgs.addAll(history.stream()
                    .map(m -> new MessageDto(m.getRole(), m.getContent()))
                    .collect(Collectors.toList()));
        }

        msgs.add(new MessageDto("user", userMsg));

        return msgs;
    }

    // ... (buildTitlePrompt, getDialectInstruction, getEmotionInstruction 메서드는 기존과 동일하게 유지) ...
    public List<MessageDto> buildTitlePrompt(String userMsg, String botResponse) {
        List<MessageDto> msgs = new ArrayList<>();
        msgs.add(new MessageDto("system", "Summarize into a concise Korean title (noun form, under 15 chars)."));
        msgs.add(new MessageDto("user", "User: " + userMsg + "\nAI: " + botResponse));
        return msgs;
    }

    private String getDialectInstruction(String regionCode) {
        if (regionCode == null) regionCode = "std";
        // 이전에 드린 '개선된 사투리 프롬프트'를 그대로 쓰시면 됩니다.
        return switch (regionCode.toLowerCase()) {
            case "gs" ->
                    "You are an elderly native speaker from Busan/Gyeongsang-do. " +
                            "Speak in a polite, warm, and trustworthy tone. " +
                            "Use endings like '~입니더', '~예', '~심더' (Jondaetmal with dialect). " +
                            "Avoid using '~노' in declarative sentences; use it only for questions if necessary. " +
                            "Example: '부산에는 노인 맞춤 돌봄 서비스가 있습니더.', '치과 주치의 혜택도 챙기 보이소.'";
            case "gw" ->
                    "You are an elderly native speaker from Gangneung (Gangwon-do). " +
                            "Speak in a gentle and slow tone. " +
                            "Use endings like '~이래요', '~이랬어요', '~잖소'.";
            default ->
                    "Use standard Korean (Seoul). Polite and respectful.";
        };
    }

    private String getEmotionInstruction(String code) {
        if (code == null) code = "6";
        return switch (code) {
            case "0" -> "User is Happy. React cheerfully.";
            case "1" -> "User is Sad. Console warmly.";
            case "2" -> "User is Angry. Calm them down.";
            case "3" -> "User is Anxious. Give reassurance.";
            case "6" -> "User is Neutral. Be informative and friendly.";
            default -> "Respond kindly.";
        };
    }
}