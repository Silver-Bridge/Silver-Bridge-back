package com.silverbridge.backend.controller.mypage;

import com.silverbridge.backend.dto.mypage.MemberMyPageDto;
import com.silverbridge.backend.service.mypage.MemberMyPageService;
import com.silverbridge.backend.service.mypage.MyPageService; // [추가] 알림 로직이 있는 서비스 임포트
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mypage/member")
public class MemberMyPageController {

    private final MemberMyPageService memberMypageService;
    private final MyPageService myPageService; // [추가] 주입 받기

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

    // [▼ 추가] 알림 설정(ON/OFF) 변경
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
}