package com.silverbridge.backend.service;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SmsVerificationManager {

	// 인증된 전화번호 목록
	private final Map<String, Boolean> verifiedNumbers = new ConcurrentHashMap<>();

	// 인증 성공 시 저장
	public void markVerified(String phoneNumber) {
		verifiedNumbers.put(phoneNumber, true);
	}

	// 인증 여부 확인
	public boolean isVerified(String phoneNumber) {
		return verifiedNumbers.getOrDefault(phoneNumber, false);
	}

	// 회원가입 완료 시 제거 (1회용)
	public void consume(String phoneNumber) {
		verifiedNumbers.remove(phoneNumber);
	}
}
