package com.silverbridge.backend.repository;

import com.silverbridge.backend.domain.RefreshToken;
import com.silverbridge.backend.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByUser(User user);
    Optional<RefreshToken> findByRefreshToken(String refreshToken); // 로그아웃 요청 => 사용자 정보 조회
	int deleteByExpiryDateBefore(LocalDateTime dateTime); // 만료일(expiryDate)이 현재 시간보다 이전인 토큰을 모두 삭제
}
