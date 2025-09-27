package com.silverbridge.backend.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;
import java.time.LocalDateTime; // LocalDateTime import

@Getter
@NoArgsConstructor
@Table(name = "refresh_tokens")
@Entity
public class RefreshToken {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// User 엔티티의 PK(id)를 참조하는 외래 키 컬럼 'user_id'가 생성됩니다.
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", unique = true, nullable = false)
	private User user;

	@Column(nullable = false)
	private String refreshToken;

	// 토큰 주기적 정리를 위해 만료일 컬럼 추가
	@Column(nullable = false)
	private LocalDateTime expiryDate;

	@Builder
	public RefreshToken(User user, String refreshToken, LocalDateTime expiryDate) {
		this.user = user;
		this.refreshToken = refreshToken;
		this.expiryDate = expiryDate;
	}

	// Refresh Token 값과 만료일을 함께 업데이트하는 메서드
	public void updateRefreshToken(String refreshToken, LocalDateTime expiryDate) {
		this.refreshToken = refreshToken;
		this.expiryDate = expiryDate;
	}
}