package com.silverbridge.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

// 카카오 토큰 서버 응답을 위한 DTO
@Getter
@Setter
public class KakaoTokenResponse {

	@JsonProperty("access_token")
	private String accessToken;

	@JsonProperty("token_type")
	private String tokenType;

	@JsonProperty("refresh_token")
	private String refreshToken;

	@JsonProperty("expires_in")
	private Integer expiresIn;

	@JsonProperty("refresh_token_expires_in")
	private Integer refreshTokenExpiresIn;
}