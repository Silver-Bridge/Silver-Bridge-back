package com.silverbridge.backend.service.calendar;

import com.silverbridge.backend.domain.User;
import com.silverbridge.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ElderAccessService {

	private final UserRepository userRepository;

	public Long getAccessibleElderId(Long userId) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("사용자 없음"));

		// 노인
		if (user.getRole().equals("ROLE_MEMBER")) {
			return user.getId();
		}

		// 보호자
		if (user.getRole().equals("ROLE_NOK")) {
			if (user.getConnectedElderId() == null) {
				throw new IllegalArgumentException("연결된 노인이 없습니다.");
			}
			return user.getConnectedElderId();
		}

		throw new SecurityException("권한이 없습니다.");
	}
}
