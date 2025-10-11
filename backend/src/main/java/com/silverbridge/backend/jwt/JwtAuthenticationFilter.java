package com.silverbridge.backend.jwt;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

		String requestURI = request.getRequestURI();

		// 소셜 회원가입 경로는 필터 스킵
		if (requestURI.startsWith("/api/users/social/kakao") ||
				requestURI.startsWith("/api/users/social/register-final")) {
			filterChain.doFilter(request, response);
			return;
		}

        // Request Header에서 JWT 토큰 추출
        String jwt = resolveToken(request);

        // 유효성 검사
		if (StringUtils.hasText(jwt) && jwtTokenProvider.validateToken(jwt)) {
			Claims claims = jwtTokenProvider.parseClaims(jwt);

			// aud=temp-user(임시 토큰)는 인증 생략
			if (!"temp-user".equals(claims.getAudience())) {
				Authentication authentication = jwtTokenProvider.getAuthentication(jwt);
				SecurityContextHolder.getContext().setAuthentication(authentication);
			}
		}

        filterChain.doFilter(request, response);
    }

    // Request Header에서 토큰 정보 추출
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(7);
        }
        return null;
    }
}