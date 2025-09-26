package com.silverbridge.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LogoutRequest {

	@NotBlank(message = "리프레시 토큰은 필수 입력 값입니다.") // 유효성 검사
	private String refreshToken;
}
