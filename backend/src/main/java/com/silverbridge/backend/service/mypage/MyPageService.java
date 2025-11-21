package com.silverbridge.backend.service.mypage;

import com.silverbridge.backend.domain.User;
import com.silverbridge.backend.dto.mypage.MyPageDto;
import com.silverbridge.backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MyPageService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	// 비밀번호 변경
	@Transactional
	public void updatePassword(String phoneNumber, MyPageDto.PasswordUpdateRequest req) {

		User user = userRepository.findByPhoneNumber(phoneNumber)
				.orElseThrow(() -> new IllegalArgumentException("사용자 없음"));

		if (!passwordEncoder.matches(req.getCurrentPassword(), user.getPassword())) {
			throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
		}

		user.setPassword(passwordEncoder.encode(req.getNewPassword()));
	}

	// 알람 설정 변경
	@Transactional
	public void updateAlarm(String phoneNumber, Boolean alarmActive) {

		User user = userRepository.findByPhoneNumber(phoneNumber)
				.orElseThrow(() -> new IllegalArgumentException("사용자 없음"));

		user.setAlarmActive(alarmActive);
	}
}