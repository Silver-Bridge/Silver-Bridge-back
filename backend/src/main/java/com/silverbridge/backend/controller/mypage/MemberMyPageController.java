package com.silverbridge.backend.controller.mypage;

import com.silverbridge.backend.domain.User;
import com.silverbridge.backend.dto.mypage.MemberMyPageDto;
import com.silverbridge.backend.repository.UserRepository;
import com.silverbridge.backend.service.UserService;
import com.silverbridge.backend.service.mypage.MemberMyPageService;
import com.silverbridge.backend.service.mypage.MyPageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mypage/member")
public class MemberMyPageController {

    private final MemberMyPageService memberMypageService;
    private final MyPageService myPageService;
	private final UserRepository userRepository;
	private final UserService userService;

	// 텍스트 사이즈 변경
    @PatchMapping("/text-size")
    public ResponseEntity<?> updateTextSize(
            Authentication authentication,
            @RequestBody MemberMyPageDto.TextSizeUpdateRequest req
    ) {
        memberMypageService.updateTextSize(authentication.getName(), req.getTextsize());
        return ResponseEntity.ok("텍스트 크기 업데이트 완료");
    }

    // 지역 변경
    @PatchMapping("/region")
    public ResponseEntity<?> updateRegion(
            Authentication authentication,
            @RequestBody MemberMyPageDto.RegionUpdateRequest req
    ) {
        memberMypageService.updateRegion(authentication.getName(), req.getRegion());
        return ResponseEntity.ok("지역 정보가 변경되었습니다.");
    }

    // 알림 설정(ON/OFF) 변경
    @PatchMapping("/alarm")
    public ResponseEntity<?> updateAlarm(
            Authentication authentication,
            @RequestBody MemberMyPageDto.AlarmUpdateRequest req
    ) {
        // MyPageService의 updateAlarm 메서드 호출 (DB 업데이트)
        myPageService.updateAlarm(authentication.getName(), req.getAlarmActive());

        String message = req.getAlarmActive() ? "알림이 켜졌습니다." : "알림이 꺼졌습니다.";
        return ResponseEntity.ok(message);
    }

	// 노인이 본인과 연결된 보호자 정보 조회
	@GetMapping("/guardian-info")
	public ResponseEntity<?> getConnectedGuardianInfo(Authentication authentication) {

		String elderPhone = authentication.getName();

		User elder = userService.findByPhoneNumber(elderPhone);

		User guardian = userRepository.findByConnectedElderId(elder.getId())
				.orElseThrow(() -> new IllegalArgumentException("연결된 보호자를 찾을 수 없습니다."));

		return ResponseEntity.ok(
				Map.of(
						"guardianId", guardian.getId(),
						"guardianName", guardian.getName(),
						"guardianPhone", guardian.getPhoneNumber()
				)
		);
	}

}