package com.silverbridge.backend.controller.chatbot;

import com.silverbridge.backend.dto.chatbot.ChatTextRequest;
import com.silverbridge.backend.dto.chatbot.ChatTextResponse;
import com.silverbridge.backend.dto.chatbot.ChatVoiceResponse;
import com.silverbridge.backend.dto.chatbot.MessageDto;
import com.silverbridge.backend.domain.chatbot.ChatSession;
import com.silverbridge.backend.service.chatbot.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;

// 챗봇 기능 관련 API 컨트롤러
@RestController
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
public class ChatController {

    // 챗봇 비즈니스 로직 처리를 위한 서비스
    private final ChatService chatService;

    // 텍스트 입력을 통한 챗봇 대화
    @PostMapping("/text")
    public ResponseEntity<ChatTextResponse> sendText(
            @Valid @RequestBody ChatTextRequest req,
            @RequestParam(value = "testUserId", required = false) Long testUserId, // [추가] 테스트용 userId 파라미터
            Principal principal) {

        // [수정] ID 결정 로직을 resolveUserId로 분리
        Long userId = resolveUserId(principal, testUserId);

        return ResponseEntity.ok(chatService.handleText(userId, req));
    }

    // 음성 입력을 통한 챗봇 대화
    @PostMapping(value = "/voice", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ChatVoiceResponse> sendVoice(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "regionCode", required = false) String regionCode,
            @RequestParam(value = "sessionId", required = false) Long sessionId, // 세션 ID 수신
            @RequestParam(value = "testUserId", required = false) Long testUserId, // [추가] 테스트용 userId 파라미터
            Principal principal) {

        // [수정] ID 결정 로직을 resolveUserId로 분리
        Long userId = resolveUserId(principal, testUserId);

        // [수정] 챗 서비스로 sessionId 전달
        return ResponseEntity.ok(chatService.handleVoice(userId, regionCode, file, sessionId));
    }

    // 특정 세션의 대화 기록 조회
    @GetMapping("/history/{sessionId}")
    public ResponseEntity<List<MessageDto>> history(
            @PathVariable Long sessionId,
            @RequestParam(value = "testUserId", required = false) Long testUserId, // [추가] 테스트용 userId 파라미터
            Principal principal) {

        Long userId = resolveUserId(principal, testUserId);
        return ResponseEntity.ok(chatService.getHistory(userId, sessionId));
    }

    // 사용자 본인의 전체 세션 목록 조회
    @GetMapping("/sessions")
    public ResponseEntity<List<ChatSession>> getSessions(
            @RequestParam(value = "testUserId", required = false) Long testUserId, // [추가] 테스트용 userId 파라미터
            Principal principal) {

        Long userId = resolveUserId(principal, testUserId);
        return ResponseEntity.ok(chatService.getSessions(userId));
    }

    // 특정 세션 삭제
    @DeleteMapping("/session/{sessionId}")
    public ResponseEntity<String> deleteSession(
            @PathVariable Long sessionId,
            @RequestParam(value = "testUserId", required = false) Long testUserId, // [추가] 테스트용 userId 파라미터
            Principal principal) {

        Long userId = resolveUserId(principal, testUserId);
        chatService.deleteSession(userId, sessionId);
        return ResponseEntity.ok("세션이 삭제되었습니다.");
    }

    /**
     * [추가] 사용자 ID 결정 로직
     * 1. JWT 토큰(Principal)에 ID가 있으면 최우선으로 사용
     * 2. 토큰이 없으면, Postman에서 전달받은 testUserId를 사용
     * 3. 둘 다 없으면 null 반환 (ChatService에서 익명 처리)
     *
     * @param principal JWT 토큰에 담긴 사용자 정보
     * @param testUserId Postman에서 전달받은 테스트 ID
     * @return 유효한 사용자 ID (로그인 ID 또는 테스트 ID), 둘 다 없으면 null 반환
     */
    private Long resolveUserId(Principal principal, Long testUserId) {
        try {
            if (principal != null && principal.getName() != null) {
                // 1. JWT 토큰(Principal)이 존재하면 최우선으로 사용
                return Long.parseLong(principal.getName());
            }
        } catch (NumberFormatException ignored) {
            // 토큰은 있지만 ID 파싱 오류가 났을 때 무시
        }

        // 2. JWT가 없으면, testUserId를 반환 (null일 수도 있음)
        return testUserId;
    }

    // [삭제] 기존의 resolveUserId(Principal principal) 메서드는 삭제됨
}