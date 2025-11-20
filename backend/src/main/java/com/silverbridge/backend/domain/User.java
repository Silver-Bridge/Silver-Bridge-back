package com.silverbridge.backend.domain;

import com.silverbridge.backend.dto.FinalRegisterRequest;
import lombok.*;
import jakarta.persistence.*;

@Entity
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "users",
		indexes = {
				@Index(name = "idx_phone_number", columnList = "phoneNumber"),
				@Index(name = "idx_kakao_id", columnList = "kakaoId")
		})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

	@Column(unique = true, nullable = true)
	private String phoneNumber;  // 일반 회원은 필수, 소셜 회원은 nullable

	@Column(nullable = true)
	private String password;    // 일반 회원은 필수, 소셜 회원은 더미 비밀번호

    private String birth;
    private Boolean gender;

	@Column(nullable = false)
    private Boolean social;

    private String region;
    private String textsize;

	@Column(unique = true, nullable = true)
	private Long kakaoId;

	@Column(nullable = false)
	private String role; // 기본: ROLE_MEMBER, 보호자: ROLE_NOK

	private Long connectedElderId;

    public static User createGeneralUser(String name, String phoneNumber, String password, String birth, Boolean gender, Boolean social, String region, String textsize) {
        return User.builder()
				.name(name)
				.phoneNumber(phoneNumber)
				.password(password)
				.birth(birth)
				.gender(gender)
				.social(false)
				.region(region)
				.textsize(textsize)
				.kakaoId(null)  // 일반 가입 회원은 null
				.role("ROLE_MEMBER")
				.connectedElderId(null)
				.build();
    }

	// 소셜 회원가입용 정적 팩토리 메서드 (Kakao Profile, 임시 필드만 포함)
	public static User createSocialUser(
			Long kakaoId, String name, String tempPhoneNumber, String tempPassword
	) {
		return User.builder()
				.kakaoId(kakaoId)
				.name(name)
				.phoneNumber(tempPhoneNumber)
				.password(tempPassword)
				.social(true) // 소셜 가입 플래그
				.role("ROLE_MEMBER")
				.connectedElderId(null)
				.build();
	}

	// 계정 통합 시 kakaoId, social 필드만 업데이트
	public void linkKakaoAccount(Long kakaoId) {
		if (this.kakaoId != null && !this.kakaoId.equals(kakaoId)) {
			throw new IllegalStateException("이미 다른 카카오 계정이 연결된 사용자입니다.");
		}
		this.kakaoId = kakaoId;
		this.social = true;
	}

	// 사용자 정보 업데이트 메서드
	public void updateSocialInfo(FinalRegisterRequest request) {
		this.name = request.getName();
		this.phoneNumber = request.getPhoneNumber();
		this.birth = request.getBirth();
		this.gender = request.getGender();
		this.region = request.getRegion();
		this.textsize = request.getTextsize();
	}

	// 보호자 - 노인 연결
	public void connectElder(Long elderId) {
		this.connectedElderId = elderId;
	}
}