// 돈나가니까 일단 더미 반환
package com.silverbridge.backend.service.chatbot;

import com.silverbridge.backend.dto.chatbot.MessageDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LlmClient {

    // LLM 챗봇 API 엔드포인트
    @Value("${chatbot.llm.endpoint:http://localhost:9100/llm/chat}")
    private String llmEndpoint;

    // 메시지 목록(프롬프트)을 LLM 서버로 보내 답변 생성
    public String chat(List<MessageDto> messages, boolean seniorFriendly) {
        // TODO: 실제 LLM API 연동 구현 필요
        return "노인 친화 답변(스텁)";
    }
}


//package com.silverbridge.backend.service.chatbot;
//
//import com.silverbridge.backend.dto.chatbot.MessageDto;
//import lombok.RequiredArgsConstructor;
//import org.springframework.ai.chat.client.ChatClient;
//import org.springframework.ai.chat.model.ChatResponse;
//import org.springframework.ai.chat.prompt.Prompt;
//import org.springframework.ai.chat.messages.Message;
//import org.springframework.ai.chat.messages.SystemMessage;
//import org.springframework.ai.chat.messages.UserMessage;
//import org.springframework.ai.chat.messages.AssistantMessage;
//import org.springframework.stereotype.Component;
//
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Component
//@RequiredArgsConstructor // [수정] final 필드(ChatClient) 주입을 위한 어노테이션
//public class LlmClient {
//
//    private final ChatClient chatClient;
//
//
//    public String chat(List<MessageDto> messages, boolean seniorFriendly) {
//
//        // 1. (우리 DTO) List<MessageDto> -> (Spring AI DTO) List<Message> 변환
//        List<Message> springAiMessages = messages.stream()
//                .map(dto -> {
//                    // DTO의 role 문자열에 따라 Spring AI의 Message 객체 타입 매핑
//                    switch (dto.getRole().toLowerCase()) {
//                        case "system":
//                            return new SystemMessage(dto.getContent());
//                        case "user":
//                            return new UserMessage(dto.getContent());
//                        case "assistant":
//                            return new AssistantMessage(dto.getContent());
//                        default:
//                            // role이 불명확할 경우 UserMessage로 처리
//                            return new UserMessage(dto.getContent());
//                    }
//                })
//                .collect(Collectors.toList());
//
//        try {
//            // 2. Spring AI ChatClient로 API 호출
//            // .prompt() : 프롬프트(메시지 목록) 설정
//            // .call() : API 호출 실행
//            // .chatResponse() : 응답 객체 반환
//            ChatResponse response = chatClient.prompt(new Prompt(springAiMessages)).call().chatResponse();
//
//            // 3. 응답 결과에서 텍스트(content) 추출
//            if (response != null && response.getResult() != null) {
//                return response.getResult().getOutput().getContent();
//            } else {
//                return "LLM 응답이 비어있습니다."; // (API는 성공했으나 응답이 빈 경우)
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            // LLM 호출 실패 시 (네트워크 오류, 401 인증 오류, 429 과금 오류 등)
//            return "죄송합니다. 답변을 생성하는 데 실패했습니다. (API 호출 오류)";
//        }
//    }
//}