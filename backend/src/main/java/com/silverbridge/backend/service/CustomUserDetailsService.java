package com.silverbridge.backend.service;

import com.silverbridge.backend.domain.User;
import com.silverbridge.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
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

		// DB에 저장된 ROLE 값 그대로 사용 ("ROLE_MEMBER", "ROLE_NOK")
		String userRole = user.getRole();

		// 권한 생성
		Collection<? extends GrantedAuthority> authorities =
				Collections.singletonList(new SimpleGrantedAuthority(userRole));

		// 스프링 시큐리티 UserDetails 생성
		return new org.springframework.security.core.userdetails.User(
				user.getPhoneNumber(),
				user.getPassword(),
				authorities
		);
	}
}