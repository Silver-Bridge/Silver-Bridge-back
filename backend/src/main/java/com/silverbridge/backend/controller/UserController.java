package com.silverbridge.backend.controller;

import com.silverbridge.backend.dto.JoinRequest;
import com.silverbridge.backend.dto.LoginRequest;
import com.silverbridge.backend.dto.TokenDto;
import com.silverbridge.backend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;

    public UserController(UserService userService, AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
    }

    // 회원가입
    @PostMapping("/join")
    public ResponseEntity<String> join(@Valid @RequestBody JoinRequest request) {
        userService.join(request);
        return ResponseEntity.ok("회원가입 성공");
    }

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<String> login(@Valid @RequestBody LoginRequest request) {
        try {
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(request.getPhoneNumber(), request.getPassword());

            Authentication authentication = authenticationManager.authenticate(authenticationToken);

            TokenDto tokenDto = userService.generateTokens(authentication.getName());

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + tokenDto.getAccessToken());
            headers.add("Refresh-Token", tokenDto.getRefreshToken());

            return new ResponseEntity<>("로그인 성공", headers, HttpStatus.OK);

        } catch (Exception e) {
            return new ResponseEntity<>("아이디 또는 비밀번호가 일치하지 않습니다.", HttpStatus.BAD_REQUEST);
        }
    }
}