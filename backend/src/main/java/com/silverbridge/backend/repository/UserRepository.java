package com.silverbridge.backend.repository;

import com.silverbridge.backend.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByPhoneNumber(String phoneNumber);
    boolean existsByPhoneNumber(String phoneNumber);
	Optional<User> findByKakaoId(Long kakaoId);
	Optional<User> findByConnectedElderId(Long connectedElderId);
	List<User> findAllByConnectedElderId(Long connectedElderId);

}
