package com.silverbridge.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

// 카카오 사용자 정보 응답을 위한 최상위 DTO
@Getter
@Setter
public class KakaoProfile {

	private Long id; // 카카오 고유 ID
	private Properties properties; // 사용자 상세 정보가 포함된 중첩 객체

	// 중첩 클래스: Properties (닉네임)
	@Getter
	@Setter
	public static class Properties {
		private String nickname;

		@JsonProperty("profile_image")
		private String profileImage;
	}
}