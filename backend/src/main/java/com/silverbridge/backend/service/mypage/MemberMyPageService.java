package com.silverbridge.backend.service.mypage;

import com.silverbridge.backend.domain.User;
import com.silverbridge.backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberMyPageService {

	private final UserRepository userRepository;

	@Transactional
	public void updateTextSize(String phoneNumber, String textsize) {
		User user = userRepository.findByPhoneNumber(phoneNumber)
				.orElseThrow(() -> new IllegalArgumentException("사용자 없음"));
		user.setTextsize(textsize);
	}

	@Transactional
	public void updateRegion(String phoneNumber, String region) {
		User user = userRepository.findByPhoneNumber(phoneNumber)
				.orElseThrow(() -> new IllegalArgumentException("사용자 없음"));
		user.setRegion(region);
	}
}
