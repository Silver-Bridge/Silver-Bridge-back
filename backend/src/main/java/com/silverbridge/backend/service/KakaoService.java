package com.silverbridge.backend.service;

import com.silverbridge.backend.dto.KakaoTokenResponse;
import com.silverbridge.backend.dto.KakaoProfile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
@Slf4j
public class KakaoService {

	// application.yml 설정 값 주입
	@Value("${kakao.client-id}") private String clientId;
	@Value("${kakao.redirect-uri}") private String redirectUri;
	@Value("${kakao.token-url}") private String tokenUrl;
	@Value("${kakao.user-info-url}") private String userInfoUrl;

	private final WebClient webClient; // WebClient 빈 주입 (Config 파일 필요)

	// 1. 인가 코드를 카카오 액세스 토큰으로 교환
	public String getKakaoAccessToken(String code) {

		MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
		body.add("grant_type", "authorization_code");
		body.add("client_id", clientId);
		body.add("redirect_uri", redirectUri);
		body.add("code", code);

		KakaoTokenResponse tokenResponse = webClient.mutate()
				.baseUrl(tokenUrl)
				.build()
				.post()
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.body(BodyInserters.fromFormData(body))
				.retrieve()
				.bodyToMono(KakaoTokenResponse.class)
				.block(); // 동기적으로 처리 (실제 환경에서는 Mono/Flux 사용 권장)

		if (tokenResponse == null || tokenResponse.getAccessToken() == null) {
			throw new RuntimeException("카카오 토큰 발급에 실패했습니다.");
		}

		return tokenResponse.getAccessToken();
	}

	// 2. 카카오 토큰으로 사용자 정보 조회
	public KakaoProfile getKakaoProfile(String kakaoAccessToken) {

		KakaoProfile profile = webClient.mutate()
				.baseUrl(userInfoUrl)
				.build()
				.get()
				.header("Authorization", "Bearer " + kakaoAccessToken)
				.retrieve()
				.bodyToMono(KakaoProfile.class)
				.block();

		if (profile == null || profile.getId() == null) {
			throw new RuntimeException("카카오 사용자 정보를 가져오는데 실패했습니다.");
		}

		return profile;
	}
}