package com.silverbridge.backend.controller.mypage;

import com.silverbridge.backend.dto.mypage.MyPageDto;
import com.silverbridge.backend.service.mypage.MyPageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mypage")
public class MyPageCommonController {

	private final MyPageService mypageService;

	// 비밀번호 변경
	@PatchMapping("/password-update")
	public ResponseEntity<?> updatePassword(
			Authentication authentication,
			@RequestBody MyPageDto.PasswordUpdateRequest request
	) {
		mypageService.updatePassword(authentication.getName(), request);
		return ResponseEntity.ok("비밀번호가 변경되었습니다.");
	}

	// 알람 설정 변경
	@PatchMapping("/alarm")
	public ResponseEntity<?> updateAlarm(
			Authentication authentication,
			@RequestBody MyPageDto.AlarmUpdateRequest request
	) {
		mypageService.updateAlarm(authentication.getName(), request.getAlarmActive());
		return ResponseEntity.ok("알람 설정이 업데이트되었습니다.");
	}
}
