package com.silverbridge.backend.service.chatbot;

import com.silverbridge.backend.dto.chatbot.MessageDto;
import com.silverbridge.backend.dto.chatbot.SearchResDto;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class PromptBuilder {

    // [복구됨] 검색 키워드 정의 (원래 코드에서 가져옴)
    private static final String[] SEARCH_KEYWORDS = {
            "복지", "혜택", "지원금", "정책", "센터",
            "추천", "어디", "찾아줘", "알려줘", "병원", "약국",
            "뉴스", "정보", "어떻게"
    };

    // [복구됨] 검색 필요 여부 판단 (원래 코드에서 가져옴)
    public boolean isSearchNeeded(String userMsg) {
        if (userMsg == null || userMsg.isBlank()) return false;
        for (String keyword : SEARCH_KEYWORDS) {
            if (userMsg.contains(keyword)) return true;
        }
        return false;
    }

    /**
     * [병합됨] 파라미터 6개 (검색 결과 포함)
     * - history, userMsg, emotionCode, regionCode, seniorFriendly: 수정 코드 기반
     * - searchResults: 원래 코드 기반
     */
    public List<MessageDto> build(List<MessageDto> history, String userMsg, String emotionCode, String regionCode, boolean seniorFriendly, List<SearchResDto> searchResults) {
        List<MessageDto> msgs = new ArrayList<>();
        StringBuilder systemPrompt = new StringBuilder();

        boolean hasSearchInfo = (searchResults != null && !searchResults.isEmpty());

        if (seniorFriendly) {
            // 1. 기본 페르소나 (수정 코드 기반)
            systemPrompt.append("You are 'SilverBridge', a professional and warm-hearted AI companion for seniors. ");

            // 2. [핵심] 지역 코드에 따른 말투(사투리) 지침 적용 (수정 코드 기반)
            String dialectInstruction = getDialectInstruction(regionCode);
            systemPrompt.append(dialectInstruction).append(" ");

            // 3. [복구됨] 검색 결과(RAG) 주입 및 처리 지침
            if (hasSearchInfo) {
                systemPrompt.append("\n### [Reference Information] ###\n");
                systemPrompt.append("You MUST answer based on the search results below.\n");
                systemPrompt.append("1. Extract specific program names, locations, or benefits.\n");
                systemPrompt.append("2. Do NOT generalize. Mention specific names found in the results.\n");
                // [중요] 정보는 정확하게 하되, 말투는 위에서 정의한 사투리로 변환 지시
                systemPrompt.append("3. IMPORTANT: Convert the explanation into the defined dialect/tone above, but keep the proper nouns (names) accurate.\n");

                for (SearchResDto item : searchResults) {
                    systemPrompt.append(String.format("- %s : %s\n", item.getTitle(), item.getDescription()));
                }
                systemPrompt.append("### End of Reference ###\n");
            }

            // 4. 대화 가이드라인 (병합됨)
            systemPrompt.append("\n[Response Guidelines]\n");
            systemPrompt.append("- Always respond in Korean. Use simple words and kind sentences.\n");

            // 길이 조정: 검색 정보가 있으면 좀 더 길게, 아니면 짧게
            if (hasSearchInfo) {
                systemPrompt.append("- Since you are explaining information, you can write up to 3~4 sentences.\n");
                systemPrompt.append("- Make sure the user clearly understands the specific benefits.\n");
            } else {
                systemPrompt.append("- Keep the response concise, using 1 or 2 short sentences suitable for speech.\n");
            }

            // 5. 감정별 행동 지침 (수정 코드 기반)
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
     * [수정 코드 유지] 지역 코드별 사투리 지침 (Native Speaker Persona)
     */
    private String getDialectInstruction(String regionCode) {
        if (regionCode == null) regionCode = "std";

        return switch (regionCode.toLowerCase()) {
            // 경상도
            case "gs" ->
                    "You are an elderly native speaker from Busan/Gyeongsang-do. " +
                            "Always answer in Korean, and use strong, natural Gyeongsang dialect. " +
                            "End sentences with '~예', '~심더', '~능교?', '~아이가', '~다'. " +
                            "Use dialect words like '마' (hey), '억수로' (very), '단디' (firmly). " +
                            "Tone: Blunt but warm (Tsundere style). " +
                            // [🔥 핵심 강화] 쉼표를 이용한 강제 휴지(Pause) 주입
                            "CRITICAL INSTRUCTION FOR TTS RHYTHM: " +
                            "Insert commas (,) frequently between words to create distinctive pauses. " +
                            "Don't worry about correct punctuation grammar; focus on the sound rhythm. " +
                            "Example: '아이고, 어르신, 오늘 날씨가, 억수로, 춥네예.' " +
                            "Example: '밥은, 묵었나? 건강, 단디, 챙기라.'";

            // 강원도
            case "gw" ->
                    "You are a gentle neighbor from Gangneung (Gangwon-do). " +
                            "Use natural Gangwon dialect endings like '~드래요', '~래요', '~잖소', '~이오'. " +
                            "Tone: Very slow, relaxed, and rustic. " +
                            // [🔥 핵심 강화] 말줄임표와 쉼표를 이용한 호흡 늘리기
                            "CRITICAL INSTRUCTION FOR TTS RHYTHM: " +
                            "Use ellipses (...) and commas (,) extensively to elongate the sound and create a slow tempo. " +
                            "Example: '어르신... 식사는... 하셨드래요?, 날씨가, 참, 좋잖소...' " +
                            "Example: '거기... 아프면, 안되는데... 병원은, 가보셨소?'";

            // 표준어
            default ->
                    "Use standard Korean (Seoul dialect). " +
                            "Tone: Polite, respectful, and soft like a kind daughter/son. " +
                            "Use honorifics (Jondaetmal) properly. " +
                            "Use commas naturally to allow the listener to understand clearly.";
        };
    }

    /**
     * [수정 코드 유지 + 피드백 반영] 감정 지침
     */
    private String getEmotionInstruction(String code) {
        if (code == null) code = "6";

        return switch (code) {
            case "0" -> "User is highly positive/happy. Share their joy, actively affirm them, and amplify the cheerful atmosphere.";
            case "1" -> "User is sad/hurt. Offer deep empathy and gentle consolation. Acknowledge their pain first.";
            case "2" -> "User is angry. Do not contradict. Acknowledge their anger immediately, then calm them with a composed tone.";
            case "3" -> "User is anxious/fearful. Provide reassurance with a firm, trustworthy tone (e.g., 'Don't worry, I will help').";
            case "4" -> "User is surprised. Use a careful, calm tone to help the user regain composure.";
            // [피드백 반영] Disgust 처리 개선: 화제 전환보다는 공감 먼저
            case "5" -> "User is expressing disgust/discomfort. Acknowledge their discomfort first, show concern, and then gently suggest a solution.";
            case "6" -> "User is neutral. Introduce friendly, useful daily topics (health, weather) to maintain conversation.";
            default -> "Emotion code is unclear. Respond kindly and warmly.";
        };
    }
}