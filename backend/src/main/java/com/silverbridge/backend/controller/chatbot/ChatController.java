package com.silverbridge.backend.controller.chatbot;

import com.silverbridge.backend.domain.User;
import com.silverbridge.backend.domain.chatbot.ChatSession;
import com.silverbridge.backend.dto.chatbot.ChatTextRequest;
import com.silverbridge.backend.dto.chatbot.ChatTextResponse;
import com.silverbridge.backend.dto.chatbot.ChatVoiceResponse;
import com.silverbridge.backend.dto.chatbot.MessageDto;
import com.silverbridge.backend.repository.UserRepository;
import com.silverbridge.backend.service.chatbot.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final UserRepository userRepository;

    @PostMapping("/text")
    public ResponseEntity<ChatTextResponse> sendText(
            @Valid @RequestBody ChatTextRequest req,
            @RequestParam(value = "testUserId", required = false) Long testUserId,
            Principal principal) {

        Long userId = resolveUserId(principal, testUserId);
        return ResponseEntity.ok(chatService.handleText(userId, req));
    }

    @PostMapping(value = "/voice", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ChatVoiceResponse> sendVoice(
            @RequestParam("file") MultipartFile file,
            // [확인] 여기서 regionCode를 받아서 Service로 넘겨줍니다.
            @RequestParam(value = "regionCode", required = false) String regionCode,
            @RequestParam(value = "sessionId", required = false) Long sessionId,
            @RequestParam(value = "testUserId", required = false) Long testUserId,
            Principal principal) {

        Long userId = resolveUserId(principal, testUserId);
        return ResponseEntity.ok(chatService.handleVoice(userId, regionCode, file, sessionId));
    }

    @GetMapping("/history/{sessionId}")
    public ResponseEntity<List<MessageDto>> history(
            @PathVariable Long sessionId,
            @RequestParam(value = "testUserId", required = false) Long testUserId,
            Principal principal) {

        Long userId = resolveUserId(principal, testUserId);
        return ResponseEntity.ok(chatService.getHistory(userId, sessionId));
    }

    @GetMapping("/sessions")
    public ResponseEntity<List<ChatSession>> getSessions(
            @RequestParam(value = "testUserId", required = false) Long testUserId,
            Principal principal) {

        Long userId = resolveUserId(principal, testUserId);
        return ResponseEntity.ok(chatService.getSessions(userId));
    }

    @DeleteMapping("/session/{sessionId}")
    public ResponseEntity<String> deleteSession(
            @PathVariable Long sessionId,
            @RequestParam(value = "testUserId", required = false) Long testUserId,
            Principal principal) {

        Long userId = resolveUserId(principal, testUserId);
        chatService.deleteSession(userId, sessionId);
        return ResponseEntity.ok("세션이 삭제되었습니다.");
    }

    private Long resolveUserId(Principal principal, Long testUserId) {
        if (principal != null) {
            try {
                String phoneNumber = principal.getName();
                Optional<User> user = userRepository.findByPhoneNumber(phoneNumber);
                if (user.isPresent()) {
                    return user.get().getId();
                }
            } catch (Exception e) {
                System.err.println("User ID 조회 실패: " + e.getMessage());
            }
        }
        return testUserId;
    }
}