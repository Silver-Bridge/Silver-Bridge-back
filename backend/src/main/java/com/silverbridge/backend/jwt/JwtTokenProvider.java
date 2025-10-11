package com.silverbridge.backend.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtTokenProvider {

    private final Key key;

    public JwtTokenProvider(@Value("${jwt.secret}") String secretKey) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    // Access Token 생성
    public String createAccessToken(Authentication authentication) {
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        long now = (new Date()).getTime();
        Date accessTokenExpiresIn = new Date(now + 86400000); // 24시간

        return Jwts.builder()
                .setSubject(authentication.getName())
                .claim("auth", authorities)
				.setAudience("access")
                .setExpiration(accessTokenExpiresIn)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

	// 소셜 로그인용 임시 Access Token 생성
	public String createTempAccessToken(Long kakaoId, String nickname) {
		long now = (new Date()).getTime();
		Date accessTokenExpiresIn = new Date(now + 300000); // 5분 후 만료 (임시 토큰)

		return Jwts.builder()
				.setSubject(String.valueOf(kakaoId)) // 주체(Subject)에 kakaoId 사용
				.claim("nickname", nickname)
				.claim("role", "GUEST") // 임시 회원 권한 부여
				.setAudience("temp-user")     // aud 클레임 추가
				.setExpiration(accessTokenExpiresIn)
				.signWith(key, SignatureAlgorithm.HS256)
				.compact();
	}

    // Refresh Token 생성 (Corrected `signWith` method)
    public String createRefreshToken() {
        long now = (new Date()).getTime();
        Date refreshTokenExpiresIn = new Date(now + 604800000); // 7일

        return Jwts.builder()
                .setExpiration(refreshTokenExpiresIn)
				.setAudience("refresh")
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // JWT 토큰에서 인증 정보 조회
    public Authentication getAuthentication(String accessToken) {
        Claims claims = parseClaims(accessToken);

        if (claims.get("auth") == null) {
            throw new RuntimeException("권한 정보가 없는 토큰입니다.");
        }

        Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(claims.get("auth").toString().split(","))
                        .map(SimpleGrantedAuthority::new)
                        .toList();

        UserDetails principal = new User(claims.getSubject(), "", authorities);
        return new UsernamePasswordAuthenticationToken(principal, "", authorities);
    }

    // JWT 유효성 검증
    public boolean validateToken(String token) {
        try {
            // Corrected with new `parserBuilder` and `setSigningKey` methods
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.info("잘못된 JWT 서명입니다.");
        } catch (ExpiredJwtException e) {
            log.info("만료된 JWT 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            log.info("지원되지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException e) {
            log.info("JWT 토큰이 잘못되었습니다.");
        }
        return false;
    }

    public Claims parseClaims(String accessToken) {
        try {
            // Corrected with new `parserBuilder` and `setSigningKey` methods
            return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(accessToken).getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }

	// 임시 토큰에서 kakaoId를 추출하고 토큰의 유효성을 검증
	public Long getKakaoIdFromTempToken(String tempTokenHeader) {
		String tempToken = tempTokenHeader;

		try {
			if (tempToken.startsWith("Bearer ")) {
				tempToken = tempToken.substring(7);
			}

			// 토큰 파싱 및 유효성 검증
			Claims claims = parseClaims(tempToken);
			String kakaoIdString = claims.getSubject();

			// Subject에 저장된 kakaoId를 Long 타입으로 변환
			if (kakaoIdString == null) {
				throw new IllegalArgumentException("토큰에 kakaoId 정보가 없습니다.");
			}
			return Long.parseLong(kakaoIdString);

		} catch (Exception e) {
			log.error("임시 토큰 처리 실패: {}", e.getMessage());
			// 클라이언트에 401 UNAUTHORIZED 응답 유도
			throw new IllegalArgumentException("유효하지 않거나 만료된 임시 토큰입니다. 다시 카카오 로그인을 시도하세요.");
		}
	}

}
