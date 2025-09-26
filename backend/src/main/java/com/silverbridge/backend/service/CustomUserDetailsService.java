package com.silverbridge.backend.service;

import com.silverbridge.backend.domain.User;
import com.silverbridge.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String phoneNumber) throws UsernameNotFoundException {
        return userRepository.findByPhoneNumber(phoneNumber)
                .map(this::createUserDetails)
                .orElseThrow(() -> new UsernameNotFoundException(phoneNumber + " -> 데이터베이스에서 찾을 수 없습니다."));
    }

    private UserDetails createUserDetails(User user) {
        // Spring Security의 User 객체를 생성하여 반환
        return new org.springframework.security.core.userdetails.User(
                user.getPhoneNumber(),
                user.getPassword(),
                Collections.singletonList(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER"))
        );
    }
}