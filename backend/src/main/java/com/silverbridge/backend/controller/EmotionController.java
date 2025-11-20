package com.silverbridge.backend.controller;

import com.silverbridge.backend.dto.EmotionCountDto;
import com.silverbridge.backend.repository.UserRepository;
import com.silverbridge.backend.service.EmotionService;
import lombok.RequiredArgsConstructor;
import com.silverbridge.backend.domain.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/emotions")
public class EmotionController {

	private final EmotionService emotionService;
	private final UserRepository userRepository;

	// 1주치 감정 요약
	@GetMapping("/weekly/last")
	public ResponseEntity<?> lastWeekEmotion(Authentication authentication) {

		String phoneNumber = authentication.getName();
		User user = userRepository.findByPhoneNumber(phoneNumber)
				.orElseThrow(() -> new RuntimeException("사용자 없음"));

		Long userId = user.getId();

		return ResponseEntity.ok(
				emotionService.getLastWeekEmotionSummary(userId)
		);
	}


	// 이번 달 감정 요약
	@GetMapping("/month/current")
	public ResponseEntity<?> getEmotionSummaryCurrentMonth(Authentication authentication) {

		String phoneNumber = authentication.getName();
		User user = userRepository.findByPhoneNumber(phoneNumber)
				.orElseThrow(() -> new RuntimeException("사용자 없음"));

		Long userId = user.getId();

		int year = java.time.LocalDate.now().getYear();
		int month = java.time.LocalDate.now().getMonthValue();

		List<EmotionCountDto> result = emotionService.getEmotionSummaryMonthly(userId, year, month);

		return ResponseEntity.ok(result);
	}

	// 지난 달 감정 요약
	@GetMapping("/month/previous")
	public ResponseEntity<?> getEmotionSummaryPreviousMonth(Authentication authentication) {

		String phoneNumber = authentication.getName();
		User user = userRepository.findByPhoneNumber(phoneNumber)
				.orElseThrow(() -> new RuntimeException("사용자 없음"));

		Long userId = user.getId();

		java.time.LocalDate now = java.time.LocalDate.now();
		java.time.LocalDate prev = now.minusMonths(1);

		int year = prev.getYear();
		int month = prev.getMonthValue();

		List<EmotionCountDto> result = emotionService.getEmotionSummaryMonthly(userId, year, month);

		return ResponseEntity.ok(result);
	}
}
