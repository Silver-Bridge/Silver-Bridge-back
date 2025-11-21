package com.silverbridge.backend.controller;

import com.silverbridge.backend.domain.User;
import com.silverbridge.backend.dto.*;
import com.silverbridge.backend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<UserResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(request.getPhoneNumber(), request.getPassword());

            Authentication authentication = authenticationManager.authenticate(authenticationToken);

//            TokenDto tokenDto = userService.generateTokens(authentication.getName());
			TokenDto tokenDto = userService.generateTokens(authentication);

			User user = userService.findByPhoneNumber(request.getPhoneNumber());
			UserResponse response = UserResponse.from(user);


			HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + tokenDto.getAccessToken());
            headers.add("Refresh-Token", tokenDto.getRefreshToken());

			return ResponseEntity.ok()
					.headers(headers)
					.body(response);

		} catch (Exception e) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
    }

	// 로그아웃
	@PostMapping("/logout")
	public ResponseEntity<String> logout(@RequestBody LogoutRequest request) {
		try {
			userService.logout(request.getRefreshToken()); // DB에 저장된 리프레시 토큰 삭제
			return ResponseEntity.ok("로그아웃 성공"); // 로그아웃 성공 응답 => 프론트엔드는 로컬에 저장된 토큰을 삭제해야 함
		} catch (IllegalArgumentException ex) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage()); // 유효하지 않은 토큰인 경우 처리
		}
	}

	// 현재 로그인한 사용자 정보를 조회
	// Access Token을 Authorization 헤더에 담아 요청.
	@GetMapping("/me")
	public ResponseEntity<UserResponse> getMyInfo() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication == null || authentication.getName() == null) {
			throw new RuntimeException("인증된 사용자 정보를 찾을 수 없습니다.");
		}

		String phoneNumber = authentication.getName(); // phoneNumber

		UserResponse userInfo = userService.getUserInfo(phoneNumber);

		return ResponseEntity.ok(userInfo);
	}

}