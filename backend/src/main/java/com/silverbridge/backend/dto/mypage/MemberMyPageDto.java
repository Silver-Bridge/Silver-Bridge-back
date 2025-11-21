package com.silverbridge.backend.dto.mypage;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Setter;

public class MemberMyPageDto {

	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor
	public static class TextSizeUpdateRequest {
		private String textsize;
	}

	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor
	public static class RegionUpdateRequest {
		private String region;
	}
}
