package com.silverbridge.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.silverbridge.backend.domain.User;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
    boolean existsByPhoneNumber(String phoneNumber);
}