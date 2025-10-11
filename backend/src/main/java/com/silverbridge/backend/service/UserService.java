package com.silverbridge.backend.service;

import com.silverbridge.backend.domain.RefreshToken;
import com.silverbridge.backend.domain.User;
import com.silverbridge.backend.dto.JoinRequest;
import com.silverbridge.backend.dto.KakaoProfile;
import com.silverbridge.backend.dto.TokenDto;
import com.silverbridge.backend.jwt.JwtTokenProvider;
import com.silverbridge.backend.dto.FinalRegisterRequest;
import com.silverbridge.backend.repository.RefreshTokenRepository;
import com.silverbridge.backend.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

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

		User user = User.createGeneralUser(
				request.getName(),
				request.getPhoneNumber(),
				passwordEncoder.encode(request.getPassword()),
				request.getBirth(),
				request.getGender(),
				false,
				request.getRegion(),
				request.getTextsize()
		);
		userRepository.save(user);
	}

	// 카카오 소셜 로그인 및 회원가입 메서드
	@Transactional(readOnly = true)
	public TokenDto socialLoginOrJoin(KakaoProfile profile) {
		Long kakaoId = profile.getId();

		String nickname = Optional.ofNullable(profile.getProperties())
				.map(KakaoProfile.Properties::getNickname)
				.orElse("카카오 사용자"); // nickname이 null인 경우 fallback

		// 기존 사용자: 즉시 로컬 JWT 발급 (로그인 성공)
		Optional<User> existing = userRepository.findByKakaoId(profile.getId());
		if (existing.isPresent()) {
			log.info("기존 카카오 회원 로그인: {}", kakaoId);
			return generateTokens(existing.get());
		}

		// 신규 사용자: 임시 토큰 발급
		log.info("신규 카카오 사용자 임시 토큰 발급: ID {}", profile.getId());

		// 회원가입 정보를 담은 임시 Access Token만 생성
		// 토큰 Payload에 'kakaoId', 'role: GUEST' 등을 담아 클라이언트에 전달
		String tempAccessToken = jwtTokenProvider.createTempAccessToken(profile.getId(), nickname);
		return TokenDto.builder()
				.grantType("Bearer")
				.accessToken(tempAccessToken)
				.refreshToken(null) // Refresh Token은 null로 반환
				.build();
	}

	// 추가정보 입력 완료 후 최종 회원가입 + 통합 처리
	@Transactional
	public TokenDto completeSocialRegistration(Long kakaoId, FinalRegisterRequest request) {
		// phoneNumber로 기존 일반회원 탐색
		Optional<User> existingUser = userRepository.findByPhoneNumber(request.getPhoneNumber());

		if (existingUser.isPresent()) {
			User user = existingUser.get();

			// 이미 다른 카카오 계정과 연결돼있다면 예외
			if (user.getKakaoId() != null && !user.getKakaoId().equals(kakaoId)) {
				throw new IllegalArgumentException("이미 다른 카카오 계정과 연결된 사용자입니다.");
			}

			// 기존 일반 회원 → 카카오 계정 연동
			log.info("기존 일반 회원과 카카오 계정 통합: phoneNumber={}, kakaoId={}",
					user.getPhoneNumber(), kakaoId);

			user.linkKakaoAccount(kakaoId);
			user.setSocial(true);
			user.updateSocialInfo(request);
			userRepository.save(user);

			return generateTokens(user);
		}

		// 신규 User 생성
		User newUser = User.createSocialUser(
				kakaoId,
				request.getName(),
				request.getPhoneNumber(),
				passwordEncoder.encode(UUID.randomUUID().toString()) // 더미 비번
		);
		newUser.updateSocialInfo(request); // 기타 정보 업데이트
		userRepository.save(newUser); // DB 저장

		// 정식 JWT 발급
		log.info("신규 카카오 회원가입 완료: kakaoId={}, phoneNumber={}", kakaoId, request.getPhoneNumber());
		return generateTokens(newUser);
	}

    // 로그인 메서드는 토큰 생성만 담당
    @Transactional
    public TokenDto generateTokens(User user) {
        // JWT 토큰 생성을 위한 Authentication 객체 수동 생성
        Authentication authentication = new UsernamePasswordAuthenticationToken(user.getPhoneNumber(), user.getPassword(), Collections.emptyList());

        String accessToken = jwtTokenProvider.createAccessToken(authentication);
        String refreshToken = jwtTokenProvider.createRefreshToken();

		LocalDateTime expiryDate = LocalDateTime.now().plusDays(7); // 7일

		// DB에 리프레시 토큰 저장 (기존 토큰이 있으면 업데이트, 없으면 새로 생성)
        refreshTokenRepository.findByUser(user).ifPresentOrElse(
                token -> token.updateRefreshToken(refreshToken, expiryDate),
                () -> refreshTokenRepository.save(
						RefreshToken.builder()
								.user(user)
								.refreshToken(refreshToken)
								.expiryDate(expiryDate) // 만료일 설정
								.build()
                )
        );
        return TokenDto.builder()
                .grantType("Bearer")
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

	// 일반 로그인
	@Transactional
	public TokenDto generateTokens(String phoneNumber) {
		User user = userRepository.findByPhoneNumber(phoneNumber)
				.orElseThrow(() -> new IllegalArgumentException("아이디 또는 비밀번호가 일치하지 않습니다."));

		return generateTokens(user);
	}

	// 로그아웃 메서드
	@Transactional
	public void logout(String refreshTokenValue) {
			RefreshToken refreshToken = refreshTokenRepository.findByRefreshToken(refreshTokenValue)
							.orElseThrow(() -> new IllegalArgumentException("유효하지 않은 리프레시 토큰입니다.")); // 400 Bad Request
			refreshTokenRepository.delete(refreshToken); // DB에서 리프레시 토큰 삭제
	}
}