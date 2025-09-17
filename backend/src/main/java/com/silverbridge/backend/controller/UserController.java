package com.silverbridge.backend.controller;

import com.silverbridge.backend.dto.JoinRequest;
import com.silverbridge.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/join")
    public ResponseEntity<String> join(@Valid @RequestBody JoinRequest request) {
        userService.join(request);
        return ResponseEntity.ok("회원 가입 성공");
    }
}