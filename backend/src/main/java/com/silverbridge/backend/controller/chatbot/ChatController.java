package com.silverbridge.backend.controller.chatbot;

import com.silverbridge.backend.dto.chatbot.ChatTextRequest;
import com.silverbridge.backend.dto.chatbot.ChatTextResponse;
import com.silverbridge.backend.dto.chatbot.ChatVoiceResponse;
import com.silverbridge.backend.dto.chatbot.MessageDto;
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

    @PostMapping("/text")
    public ResponseEntity<ChatTextResponse> sendText(@Valid @RequestBody ChatTextRequest req,
                                                     Principal principal) {
        Long userId = resolveUserId(principal);
        return ResponseEntity.ok(chatService.handleText(userId, req));
    }

    @PostMapping(value = "/voice", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ChatVoiceResponse> sendVoice(@RequestParam("file") MultipartFile file,
                                                       @RequestParam(value = "regionCode", required = false) String regionCode,
                                                       Principal principal) {
        Long userId = resolveUserId(principal);
        return ResponseEntity.ok(chatService.handleVoice(userId, regionCode, file));
    }

    @GetMapping("/history/{sessionId}")
    public ResponseEntity<List<MessageDto>> history(@PathVariable Long sessionId,
                                                    Principal principal) {
        Long userId = resolveUserId(principal);
        return ResponseEntity.ok(chatService.getHistory(userId, sessionId));
    }

    private Long resolveUserId(Principal principal) {
        try {
            if (principal != null && principal.getName() != null) {
                return Long.parseLong(principal.getName());
            }
        } catch (NumberFormatException ignored) {}
        return 1L; // 기본값
    }
}
