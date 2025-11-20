package com.silverbridge.backend.service;

import com.silverbridge.backend.dto.EmotionCountDto;
import com.silverbridge.backend.repository.EmotionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmotionService {

	private final EmotionRepository emotionRepository;

	// 최근 1주 감정 요약
	public List<EmotionCountDto> getLastWeekEmotionSummary(Long userId) {

		LocalDate today = LocalDate.now();

		// 이번 주 월요일
		LocalDate thisWeekMonday = today.with(DayOfWeek.MONDAY);

		// 지난 주 월요일
		LocalDate lastWeekMonday = thisWeekMonday.minusWeeks(1);

		// 지난 주 일요일
		LocalDate lastWeekSunday = thisWeekMonday.minusDays(1);

		LocalDateTime start = lastWeekMonday.atStartOfDay();
		LocalDateTime end = lastWeekSunday.atTime(23, 59, 59);

		return emotionRepository.getEmotionSummaryNative(userId, start, end)
				.stream()
				.map(p -> new EmotionCountDto(p.getEmotion(), p.getCnt()))
				.toList();
	}


	// 특정 월의 감정 요약
	public List<EmotionCountDto> getEmotionSummaryMonthly(Long userId, int year, int month) {

		LocalDateTime start = LocalDateTime.of(year, month, 1, 0, 0);
		LocalDateTime end = start.plusMonths(1).minusSeconds(1);

		return emotionRepository.getEmotionSummaryNative(userId, start, end)
				.stream()
				.map(p -> new EmotionCountDto(
						p.getEmotion(),
						p.getCnt()
				))
				.toList();
	}
}
