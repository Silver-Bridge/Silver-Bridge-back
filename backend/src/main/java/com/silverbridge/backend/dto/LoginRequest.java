/*
전화번호 인증 기능은 JWT와 별개로
추가 API를 통해 구현되므로 로그인 DTO에는 포함하지 않는다.
*/
package com.silverbridge.backend.dto;

import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Getter
@Setter
public class LoginRequest {

    @Email(message = "잘못된 이메일 양식입니다.")
    @NotBlank(message = "이메일은 필수 입력 값입니다.")
    private String email;

    @Size(min = 8, max = 20, message = "비밀번호의 길이는 최소 8자 이상 최대 20자 이하입니다.")
    @Pattern(regexp = "(?=.*[a-zA-Z])(?=.*[0-9]).*", message = "비밀번호는 문자, 숫자를 포함해야 합니다.")
    @NotBlank(message = "비밀번호는 필수 입력 값입니다.")
    private String password;

}