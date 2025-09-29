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

@RestController
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    /**
     * 텍스트 입력으로 챗봇 대화
     */
    @PostMapping("/text")
    public ResponseEntity<ChatTextResponse> sendText(@Valid @RequestBody ChatTextRequest req,
                                                     Principal principal) {
        Long userId = resolveUserId(principal);
        return ResponseEntity.ok(chatService.handleText(userId, req));
    }

    /**
     * 음성 입력으로 챗봇 대화
     */
    @PostMapping(value = "/voice", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ChatVoiceResponse> sendVoice(@RequestParam("file") MultipartFile file,
                                                       @RequestParam(value = "regionCode", required = false) String regionCode,
                                                       Principal principal) {
        Long userId = resolveUserId(principal);
        return ResponseEntity.ok(chatService.handleVoice(userId, regionCode, file));
    }

    /**
     * 특정 세션 대화 히스토리 조회
     */
    @GetMapping("/history/{sessionId}")
    public ResponseEntity<List<MessageDto>> history(@PathVariable Long sessionId,
                                                    Principal principal) {
        Long userId = resolveUserId(principal);
        return ResponseEntity.ok(chatService.getHistory(userId, sessionId));
    }

    /**
     * 사용자 자신의 세션 목록 조회
     */
    @GetMapping("/sessions")
    public ResponseEntity<List<ChatSession>> getSessions(Principal principal) {
        Long userId = resolveUserId(principal);
        return ResponseEntity.ok(chatService.getSessions(userId));
    }

    /**
     * 특정 세션 삭제
     */
    @DeleteMapping("/session/{sessionId}")
    public ResponseEntity<String> deleteSession(@PathVariable Long sessionId,
                                                Principal principal) {
        Long userId = resolveUserId(principal);
        chatService.deleteSession(userId, sessionId);
        return ResponseEntity.ok("세션이 삭제되었습니다.");
    }

    /**
     * JWT → userId 해석 (지금은 기본값 1L)
     */
    private Long resolveUserId(Principal principal) {
        try {
            if (principal != null && principal.getName() != null) {
                return Long.parseLong(principal.getName());
            }
        } catch (NumberFormatException ignored) {}
        return 1L; // 기본값
    }
}
