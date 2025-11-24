package com.silverbridge.backend.controller.chatbot;

import com.silverbridge.backend.domain.User;
import com.silverbridge.backend.domain.chatbot.ChatSession;
import com.silverbridge.backend.dto.chatbot.ChatTextRequest;
import com.silverbridge.backend.dto.chatbot.ChatTextResponse;
import com.silverbridge.backend.dto.chatbot.ChatVoiceResponse;
import com.silverbridge.backend.dto.chatbot.MessageDto;
import com.silverbridge.backend.repository.UserRepository; // [중요] DB 조회용
import com.silverbridge.backend.service.chatbot.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;
import java.util.Optional; // [중요] Optional 에러 해결을 위해 필수!

@RestController
// [참고] 테스트가 끝나면 나중에 "/api/chatbot"으로 되돌리세요
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    // ▼▼▼ [중요] 이 줄이 없어서 userRepository 에러가 났던 것입니다. 꼭 넣어주세요!
    private final UserRepository userRepository;
    // ▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲

    // 텍스트 입력을 통한 챗봇 대화
    @PostMapping("/text")
    public ResponseEntity<ChatTextResponse> sendText(
            @Valid @RequestBody ChatTextRequest req,
            @RequestParam(value = "testUserId", required = false) Long testUserId,
            Principal principal) {

        Long userId = resolveUserId(principal, testUserId);
        return ResponseEntity.ok(chatService.handleText(userId, req));
    }

    // 음성 입력을 통한 챗봇 대화
    @PostMapping(value = "/voice", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ChatVoiceResponse> sendVoice(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "regionCode", required = false) String regionCode,
            @RequestParam(value = "sessionId", required = false) Long sessionId,
            @RequestParam(value = "testUserId", required = false) Long testUserId,
            Principal principal) {

        Long userId = resolveUserId(principal, testUserId);
        return ResponseEntity.ok(chatService.handleVoice(userId, regionCode, file, sessionId));
    }

    // 특정 세션의 대화 기록 조회
    @GetMapping("/history/{sessionId}")
    public ResponseEntity<List<MessageDto>> history(
            @PathVariable Long sessionId,
            @RequestParam(value = "testUserId", required = false) Long testUserId,
            Principal principal) {

        Long userId = resolveUserId(principal, testUserId);
        return ResponseEntity.ok(chatService.getHistory(userId, sessionId));
    }

    // 사용자 본인의 전체 세션 목록 조회
    @GetMapping("/sessions")
    public ResponseEntity<List<ChatSession>> getSessions(
            @RequestParam(value = "testUserId", required = false) Long testUserId,
            Principal principal) {

        Long userId = resolveUserId(principal, testUserId);
        return ResponseEntity.ok(chatService.getSessions(userId));
    }

    // 특정 세션 삭제
    @DeleteMapping("/session/{sessionId}")
    public ResponseEntity<String> deleteSession(
            @PathVariable Long sessionId,
            @RequestParam(value = "testUserId", required = false) Long testUserId,
            Principal principal) {

        Long userId = resolveUserId(principal, testUserId);
        chatService.deleteSession(userId, sessionId);
        return ResponseEntity.ok("세션이 삭제되었습니다.");
    }

    /**
     * 사용자 ID 결정 로직
     * 토큰(Principal)의 전화번호로 DB를 조회하여 진짜 ID(Long)를 반환
     */
    private Long resolveUserId(Principal principal, Long testUserId) {
        if (principal != null) {
            try {
                // 1. 토큰에서 전화번호 추출 (예: "010-1234-5678")
                String phoneNumber = principal.getName();

                // 2. 전화번호로 DB에서 유저 찾기
                // (findByPhoneNumber는 UserRepository에 정의되어 있어야 함)
                Optional<User> user = userRepository.findByPhoneNumber(phoneNumber);

                // 3. 유저가 존재하면 진짜 ID(PK) 반환
                if (user.isPresent()) {
                    return user.get().getId();
                }
            } catch (Exception e) {
                // DB 조회 중 에러가 나면 로그 남기고 testUserId 사용
                System.err.println("User ID 조회 실패: " + e.getMessage());
            }
        }

        // 4. 토큰이 없거나 유저를 못 찾으면 테스트 ID 반환
        return testUserId;
    }
}