package com.silverbridge.backend.controller;

import com.silverbridge.backend.domain.User;
import com.silverbridge.backend.dto.FinalRegisterRequest;
import com.silverbridge.backend.dto.TokenDto;
import com.silverbridge.backend.dto.UserResponse;
import com.silverbridge.backend.jwt.JwtTokenProvider;
import com.silverbridge.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users/social")
@RequiredArgsConstructor
public class SocialInfoController {

	private final UserService userService;
	private final JwtTokenProvider jwtTokenProvider; // 임시 토큰 검증용

	// 최종 회원가입 및 본 토큰 발급 API
	@PostMapping("/register-final")
	public ResponseEntity<UserResponse> finalRegister(
			@RequestHeader("Authorization") String tempTokenHeader,
			@Valid @RequestBody FinalRegisterRequest request
	) {
		try {
			// 임시 토큰에서 kakaoId 추출 및 유효성 검증
			Long kakaoId = jwtTokenProvider.getKakaoIdFromTempToken(tempTokenHeader);

			// UserService에서 최종 DB 저장 및 본 토큰 발급
			TokenDto finalToken = userService.completeSocialRegistration(kakaoId, request);

			User user = userService.findByPhoneNumber(request.getPhoneNumber());
			UserResponse response = UserResponse.from(user);

			// 본 토큰을 헤더에 담아 응답
			HttpHeaders headers = new HttpHeaders();
			headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + finalToken.getAccessToken());
			headers.add("Refresh-Token", finalToken.getRefreshToken());

			return ResponseEntity.ok()
					.headers(headers)
					.body(response);

		} catch (IllegalArgumentException e) {
			return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED); // 401 UNAUTHORIZED
		} catch (Exception e) {
			// 기타 서버 오류
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}