package com.silverbridge.backend.service;

import com.silverbridge.backend.domain.User;
import com.silverbridge.backend.exception.CustomException;
import com.silverbridge.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ConnectElderService {

	private final UserRepository userRepository;

	@Transactional
	public void connectElder(Long guardianId, String elderPhone) {

		User guardian = userRepository.findById(guardianId)
				.orElseThrow(() -> new CustomException("보호자 계정 없음"));

		if (!"ROLE_NOK".equals(guardian.getRole())) {
			throw new CustomException("보호자 계정이 아닙니다.");
		}

		User elder = userRepository.findByPhoneNumber(elderPhone)
				.orElseThrow(() -> new CustomException("노인 계정 없음"));

		if (!"ROLE_MEMBER".equals(elder.getRole())) {
			throw new CustomException("해당 번호는 노인(role=MEMBER) 계정이 아님");
		}

		if (guardian.getConnectedElderId() != null) {
			throw new CustomException("이미 노인과 연결된 보호자입니다.");
		}

		guardian.connectElder(elder.getId());
	}
}
