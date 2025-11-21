package com.silverbridge.backend.dto;

import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.*;

@Getter
@Setter
public class JoinRequest {

    @NotBlank(message = "이름은 필수 입력 값입니다.")
    @Pattern(regexp = "^[가-힣]+$", message = "이름은 한글만 가능합니다.")
    private String name;

    @Size(min = 8, max = 20, message = "비밀번호의 길이는 최소 8자 이상 최대 20자 이하입니다.")
    @Pattern(regexp = "(?=.*[a-zA-Z])(?=.*[0-9]).*", message = "비밀번호는 문자, 숫자를 포함해야 합니다.")
    private String password;

    @Pattern(regexp = "^010-[0-9]{4}-[0-9]{4}$", message = "전화번호 양식은 XXX-XXXX-XXXX로 입력해야 합니다.")
    private String phoneNumber;

    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "잘못된 생년월일 양식입니다.")
    private String birth;

    @NotNull(message = "성별을 체크하셔야 합니다.")
    private Boolean gender;

    @NotNull(message = "가입 유형은 kakao 또는 basic만 허용됩니다.")
    private Boolean social;

    @NotBlank(message = "지역을 체크하셔야 합니다.")
    private String region;

    @NotBlank(message = "글자 크기는 필수 입력 값입니다.")
    private String textsize;

	@NotBlank(message = "노인/보호자 권한은 필수 입력 값입니다.")
	private String role;
}
