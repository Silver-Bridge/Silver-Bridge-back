package com.silverbridge.backend.service;

import com.silverbridge.backend.domain.RefreshToken;
import com.silverbridge.backend.domain.User;
import com.silverbridge.backend.dto.JoinRequest;
import com.silverbridge.backend.dto.TokenDto;
import com.silverbridge.backend.jwt.JwtTokenProvider;
import com.silverbridge.backend.repository.RefreshTokenRepository;
import com.silverbridge.backend.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Collections;
import org.springframework.security.core.Authentication;


@Slf4j
@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public UserService(UserRepository userRepository, RefreshTokenRepository refreshTokenRepository, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

		// 회원가입 메서드
    @Transactional
    public void join(JoinRequest request) {
        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new IllegalArgumentException("이미 존재하는 전화번호입니다.");
        }

        User user = new User(
                request.getName(),
								request.getPhoneNumber(),
                passwordEncoder.encode(request.getPassword()),
                request.getBirth(),
                request.getGender(),
                request.getSocial(),
                request.getRegion(),
                request.getTextsize()
        );
        userRepository.save(user);
    }

    // 로그인 메서드는 토큰 생성만 담당
    @Transactional
    public TokenDto generateTokens(String phoneNumber) {
        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new IllegalArgumentException("아이디 또는 비밀번호가 일치하지 않습니다."));

        // JWT 토큰 생성을 위한 Authentication 객체 수동 생성
        Authentication authentication = new UsernamePasswordAuthenticationToken(user.getPhoneNumber(), user.getPassword(), Collections.emptyList());

        String accessToken = jwtTokenProvider.createAccessToken(authentication);
        String refreshToken = jwtTokenProvider.createRefreshToken();

        refreshTokenRepository.findByUser(user).ifPresentOrElse(
                token -> token.updateRefreshToken(refreshToken),
                () -> refreshTokenRepository.save(
                        RefreshToken.builder()
                                .user(user)
                                .refreshToken(refreshToken)
                                .build()
                )
        );
        return TokenDto.builder()
                .grantType("Bearer")
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

		// 로그아웃 메서드
		@Transactional
		public void logout(String refreshTokenValue) {
				RefreshToken refreshToken = refreshTokenRepository.findByRefreshToken(refreshTokenValue)
								.orElseThrow(() -> new IllegalArgumentException("유효하지 않은 리프레시 토큰입니다.")); // 400 Bad Request
				refreshTokenRepository.delete(refreshToken); // DB에서 리프레시 토큰 삭제
		}
}