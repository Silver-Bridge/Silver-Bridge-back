package com.silverbridge.backend.repository;

import com.silverbridge.backend.domain.RefreshToken;
import com.silverbridge.backend.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByUser(User user);
    Optional<RefreshToken> findByRefreshToken(String refreshToken); // ← 메서드명 주의
    void deleteByUser(User user);
}
