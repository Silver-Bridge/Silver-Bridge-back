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
    public ResponseEntity<ChatTextResponse> sendText(@Valid @RequestBody ChatTextRequest req,
                                                     Principal principal) {
        Long userId = resolveUserId(principal);
        return ResponseEntity.ok(chatService.handleText(userId, req));
    }

    // 음성 입력을 통한 챗봇 대화
    @PostMapping(value = "/voice", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ChatVoiceResponse> sendVoice(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "regionCode", required = false) String regionCode,
            @RequestParam(value = "sessionId", required = false) Long sessionId, // [수정] sessionId 파라미터 추가
            Principal principal) {

        Long userId = resolveUserId(principal);
        // [수정] 챗 서비스로 sessionId 전달
        return ResponseEntity.ok(chatService.handleVoice(userId, regionCode, file, sessionId));
    }

    // 특정 세션의 대화 기록 조회
    @GetMapping("/history/{sessionId}")
    public ResponseEntity<List<MessageDto>> history(@PathVariable Long sessionId,
                                                    Principal principal) {
        Long userId = resolveUserId(principal);
        return ResponseEntity.ok(chatService.getHistory(userId, sessionId));
    }

    // 사용자 본인의 전체 세션 목록 조회
    @GetMapping("/sessions")
    public ResponseEntity<List<ChatSession>> getSessions(Principal principal) {
        Long userId = resolveUserId(principal);
        return ResponseEntity.ok(chatService.getSessions(userId));
    }

    // 특정 세션 삭제
    @DeleteMapping("/session/{sessionId}")
    public ResponseEntity<String> deleteSession(@PathVariable Long sessionId,
                                                Principal principal) {
        Long userId = resolveUserId(principal);
        chatService.deleteSession(userId, sessionId);
        return ResponseEntity.ok("세션이 삭제되었습니다.");
    }

    // Principal 객체에서 userId 추출 (임시 기본값: 1L)
    private Long resolveUserId(Principal principal) {
        try {
            if (principal != null && principal.getName() != null) {
                return Long.parseLong(principal.getName());
            }
        } catch (NumberFormatException ignored) {}
        return 1L; // 기본값
    }
}