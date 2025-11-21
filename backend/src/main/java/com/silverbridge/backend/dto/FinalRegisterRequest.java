package com.silverbridge.backend.dto;

import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

// 소셜 가입시 필수 추가 정보를 받기 위한 DTO (입력폼)
@Getter
@Setter
public class FinalRegisterRequest {

	@NotBlank(message = "이름은 필수 입력 값입니다.")
	@Pattern(regexp = "^[가-힣]+$", message = "이름은 한글만 가능합니다.")
	private String name;

	@Pattern(regexp = "^010-[0-9]{4}-[0-9]{4}$", message = "전화번호 양식은 XXX-XXXX-XXXX로 입력해야 합니다.")
	private String phoneNumber;

	@Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "잘못된 생년월일 양식입니다.")
	@NotBlank(message = "생년월일은 필수 입력 값입니다.")
	private String birth;

	@NotNull(message = "성별을 체크하셔야 합니다.")
	private Boolean gender;

	@NotBlank(message = "지역을 체크하셔야 합니다.")
	private String region;

	@NotBlank(message = "글자 크기는 필수 입력 값입니다.")
	private String textsize;

	@NotBlank(message = "노인/보호자 권한은 필수 입력 값입니다.")
	private String role;
}