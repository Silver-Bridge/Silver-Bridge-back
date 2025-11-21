package com.silverbridge.backend.dto.mypage;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class MyPageDto {

	@Getter @Setter
	@NoArgsConstructor
	@AllArgsConstructor
	public static class AlarmUpdateRequest {
		private Boolean alarmActive;
	}

	@Getter @Setter
	@NoArgsConstructor
	@AllArgsConstructor
	public static class PasswordUpdateRequest {
		private String currentPassword;
		private String newPassword;
	}
}

