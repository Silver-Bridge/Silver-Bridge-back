package com.silverbridge.backend.domain;

import com.silverbridge.backend.dto.FinalRegisterRequest;
import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;

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

    private String birth;   // 형식: "YYYYMMDD" 또는 "YYYY-MM-DD" 가정
    private Boolean gender; // true: 남자(M), false: 여자(F) 가정

    @Column(nullable = false)
    private Boolean social;

    private String region;

    @Column(nullable = true) // 보호자는 크기 변경 불가.
    private String textsize;

    @Column(unique = true, nullable = true)
    private Long kakaoId;

    @Column(nullable = false)
    private String role; // 노인: ROLE_MEMBER, 보호자: ROLE_NOK

    private Long connectedElderId;

    @Column(nullable = false)
    @Builder.Default
    private Boolean alarmActive = true;


    public static User createGeneralUser(
            String name,
            String phoneNumber,
            String password,
            String birth,
            Boolean gender,
            Boolean social,
            String region,
            String textsize,
            String role) {

        return User.builder()
                .name(name)
                .phoneNumber(phoneNumber)
                .password(password)
                .birth(birth)
                .gender(gender)
                .social(false)
                .region(region)
                .textsize(textsize)
                .kakaoId(null)
                .role(role)
                .connectedElderId(null)
                .alarmActive(true)
                .build();
    }

    public static User createSocialUser(
            Long kakaoId, String name, String tempPhoneNumber, String tempPassword
    ) {
        return User.builder()
                .kakaoId(kakaoId)
                .name(name)
                .phoneNumber(tempPhoneNumber)
                .password(tempPassword)
                .birth(null)
                .gender(false) // 기본값
                .textsize(null)
                .social(true)
                .role("ROLE_MEMBER")
                .region(null)
                .connectedElderId(null)
                .alarmActive(true)
                .build();
    }

    public void linkKakaoAccount(Long kakaoId) {
        if (this.kakaoId != null && !this.kakaoId.equals(kakaoId)) {
            throw new IllegalStateException("이미 다른 카카오 계정이 연결된 사용자입니다.");
        }
        this.kakaoId = kakaoId;
        this.social = true;
    }

    public void updateSocialInfo(FinalRegisterRequest request) {
        this.name = request.getName();
        this.phoneNumber = request.getPhoneNumber();
        this.birth = request.getBirth();
        this.gender = request.getGender();
        this.region = request.getRegion();
        this.textsize = request.getTextsize();
        this.role = request.getRole();
    }

    public void connectElder(Long elderId) {
        this.connectedElderId = elderId;
    }

    // ==========================================
    // [추가] TTS 및 챗봇을 위한 편의 메서드 (Helper Methods)
    // ==========================================

    /**
     * 생년월일(String)을 파싱하여 만 나이를 계산합니다.
     * 형식이 올바르지 않거나 비어있으면 기본값 70세를 반환합니다.
     */
    public int getAge() {
        if (this.birth == null || this.birth.isBlank()) {
            return 70; // 정보 없음 기본값
        }
        try {
            // 숫자만 남기고 제거 (2024-01-01 -> 20240101)
            String cleanBirth = this.birth.replaceAll("[^0-9]", "");

            // 길이가 8자리(YYYYMMDD)가 아니면 기본값 반환
            if (cleanBirth.length() != 8) return 70;

            int year = Integer.parseInt(cleanBirth.substring(0, 4));
            int month = Integer.parseInt(cleanBirth.substring(4, 6));
            int day = Integer.parseInt(cleanBirth.substring(6, 8));

            LocalDate birthDate = LocalDate.of(year, month, day);
            return Period.between(birthDate, LocalDate.now()).getYears();
        } catch (Exception e) {
            return 70; // 파싱 실패 시 기본값
        }
    }

    /**
     * Boolean 성별을 TTS 클라이언트가 이해하는 코드("M", "F")로 변환합니다.
     * (여기서는 true=Male, false=Female로 가정했습니다. 반대라면 순서를 바꾸세요)
     */
    public String getGenderCode() {
        if (this.gender == null) return "F"; // 기본값
        return this.gender ? "M" : "F";
    }
}