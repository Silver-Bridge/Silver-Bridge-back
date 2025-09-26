package com.silverbridge.backend.service;

import com.silverbridge.backend.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenCleanupService {

	private final RefreshTokenRepository refreshTokenRepository;

	/**
	 * 매일 새벽 4시 정각에 실행하여 만료된 토큰을 DB에서 정리한다.
	 * Cron 표현식: 초 분 시 일 월 요일
	 */
	@Scheduled(cron = "0 0 4 * * ?") //
	@Transactional
	public void cleanupExpiredTokens() {
		log.info("만료된 리프레시 토큰 정리 작업 시작.");

		// 현재 시간을 기준으로 만료일(expiryDate)이 지난 모든 토큰을 조회하여 삭제
		int deletedCount = refreshTokenRepository.deleteByExpiryDateBefore(LocalDateTime.now());

		log.info("만료된 리프레시 토큰 정리 작업 완료. 삭제된 토큰 수: {}", deletedCount);
	}
}