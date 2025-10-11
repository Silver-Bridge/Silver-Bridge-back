package com.silverbridge.backend.controller;

import com.silverbridge.backend.dto.KakaoProfile;
import com.silverbridge.backend.dto.TokenDto;
import com.silverbridge.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@RestController
@RequestMapping("/api/users/social")
@RequiredArgsConstructor
public class KakaoAuthController {

	private final UserService userService;

	/*
	 * 카카오 로그인 콜백 or 프론트에서 Access Token 전달받는 엔드포인트
	 * 프론트엔드가 카카오 access_token을 서버에 넘겨주면
	 * 서버는 카카오 API로 사용자 정보 조회 후 socialLoginOrJoin() 호출
	 */
	@PostMapping("/kakao")
	public ResponseEntity<?> kakaoLogin(@RequestParam("accessToken") String kakaoAccessToken) {
		try {
			// 프론트에서 access_token을 받아서 카카오 사용자 정보 조회
			KakaoProfile kakaoProfile = getKakaoUserProfile(kakaoAccessToken);

			// UserService로 로그인 or 회원가입 처리
			TokenDto tokenResponse = userService.socialLoginOrJoin(kakaoProfile);

			return ResponseEntity.ok(tokenResponse);

		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body("카카오 로그인 실패: " + e.getMessage());
		}
	}

	/*
	 *  카카오 API 호출, (kapi.kakao.com/v2/user/me)
	 *  KakaoProfile DTO로 파싱
	 */
	private KakaoProfile getKakaoUserProfile(String kakaoAccessToken) {
		try {
			return WebClient.create("https://kapi.kakao.com")
					.get()
					.uri("/v2/user/me")
					.header("Authorization", "Bearer " + kakaoAccessToken)
					.retrieve()
					.bodyToMono(KakaoProfile.class)
					.block();
		} catch (WebClientResponseException e) {
			throw new RuntimeException("카카오 사용자 정보 요청 실패: " + e.getResponseBodyAsString(), e);
		} catch (Exception e) {
			throw new RuntimeException("카카오 사용자 정보 요청 중 오류 발생: " + e.getMessage(), e);
		}
	}
}
