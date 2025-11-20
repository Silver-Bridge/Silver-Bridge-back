package com.silverbridge.backend.controller.guardian;

import com.silverbridge.backend.dto.guardian.ConnectRequest;
import com.silverbridge.backend.service.ConnectElderService;
import com.silverbridge.backend.repository.UserRepository;
import com.silverbridge.backend.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/nok")
public class GuardianController {

	private final ConnectElderService connectElderService;
	private final UserRepository userRepository;

	@PostMapping("/connect")
	public ResponseEntity<?> connectElder(
			@RequestBody ConnectRequest request,
			Authentication authentication
	) {
		// 1. 보호자 로그인 정보 → phoneNumber 가져오기
		String guardianPhone = authentication.getName();

		// 2. phoneNumber → guardianId 조회
		User guardian = userRepository.findByPhoneNumber(guardianPhone)
				.orElseThrow(() -> new IllegalArgumentException("보호자 계정을 찾을 수 없습니다."));

		Long guardianId = guardian.getId();

		// 3. 서비스 호출 (elderPhone 전달)
		connectElderService.connectElder(guardianId, request.getElderPhone());

		return ResponseEntity.ok(
				Map.of("message", "노인과 성공적으로 연결되었습니다.")
		);
	}
}
