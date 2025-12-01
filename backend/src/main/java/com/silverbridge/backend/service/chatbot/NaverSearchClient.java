package com.silverbridge.backend.service.chatbot;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.silverbridge.backend.dto.chatbot.SearchResDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class NaverSearchClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${naver.client.id}")
    private String clientId;

    @Value("${naver.client.secret}")
    private String clientSecret;

    public List<SearchResDto> search(String query) {
        if (query == null || query.isBlank()) return new ArrayList<>();

        try {
            // 정확도를 위해 검색어 뒤에 핵심 키워드 추가
            String keyword = query + " 노인 복지 혜택";

            System.out.println("🚀 [NaverAPI] 검색 요청 시작. 키워드: " + keyword);
            System.out.println("   - Client ID 확인: " + (clientId != null ? "OK (앞자리:" + clientId.substring(0, 2) + "...)" : "NULL"));

            URI uri = UriComponentsBuilder
                    .fromUriString("https://openapi.naver.com")
                    .path("/v1/search/webkr.json")
                    .queryParam("query", keyword)
                    .queryParam("display", 3)
                    .queryParam("sort", "sim")
                    .encode()
                    .build()
                    .toUri();

            RequestEntity<Void> req = RequestEntity
                    .get(uri)
                    .header("X-Naver-Client-Id", clientId)
                    .header("X-Naver-Client-Secret", clientSecret)
                    .build();

            ResponseEntity<String> response = restTemplate.exchange(req, String.class);

            // [핵심] 네이버가 준 응답을 콘솔에 그대로 찍어봅니다.
            System.out.println("✅ [NaverAPI] 응답 수신 완료 (Status: " + response.getStatusCode() + ")");
            System.out.println("📄 [NaverAPI] 응답 본문: " + response.getBody());

            return parseResult(response.getBody());

        } catch (Exception e) {
            // 에러가 나면 여기서 잡힘
            System.err.println("🚨 [NaverAPI] 호출 실패! 원인: " + e.getMessage());
            e.printStackTrace(); // 자세한 에러 로그 출력
            return new ArrayList<>();
        }
    }

    private List<SearchResDto> parseResult(String jsonBody) {
        List<SearchResDto> list = new ArrayList<>();
        try {
            JsonNode root = objectMapper.readTree(jsonBody);
            JsonNode items = root.path("items");

            if (items.isArray()) {
                for (JsonNode item : items) {
                    String title = removeTags(item.path("title").asText());
                    String desc = removeTags(item.path("description").asText());
                    String link = item.path("link").asText();
                    list.add(new SearchResDto(title, desc, link));
                }
            }
            System.out.println("👉 [NaverAPI] 파싱된 결과 개수: " + list.size() + "개");
        } catch (Exception e) {
            System.err.println("⚠️ [NaverAPI] JSON 파싱 실패: " + e.getMessage());
        }
        return list;
    }

    private String removeTags(String text) {
        if (text == null) return "";
        return text.replaceAll("<[^>]*>", "").replaceAll("&quot;", "\"");
    }
}