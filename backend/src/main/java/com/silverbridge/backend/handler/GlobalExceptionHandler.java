/*
명세에 포함된 604 (닉네임), 608 (지역), 609 (가입유형) 코드는 요청 바디에 해당 필드가 없거나 DTO와 매핑되지 않기 때문에 처리 로직에서 제외함.
필요할 시, DTO에 해당 필드를 추가하고 유효성 검사 규칙을 정의할 것.
 */
package com.silverbridge.backend.handler;

import com.silverbridge.backend.exception.CustomException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("code", 600); // 명세에 없는 예외는 600대 코드로 처리함
        responseBody.put("message", "유효성 검사 실패");

		Map<String, String> fieldErrors = new HashMap<>();

		for (var error : ex.getBindingResult().getAllErrors()) {
			String fieldName = ((FieldError) error).getField();
			String errorMessage = error.getDefaultMessage();
			fieldErrors.put(fieldName, errorMessage);

			// 명세 기반 코드 매핑
			switch (fieldName) {
				case "phoneNumber" -> responseBody.put("code", 607);
				case "password" -> responseBody.put("code", 601);
				case "name" -> responseBody.put("code", 603);
				case "birth" -> responseBody.put("code", 605);
				case "gender" -> responseBody.put("code", 606);
				default -> responseBody.put("code", 600);
			}
		}

		// 필드별 상세 에러메시지 포함
		responseBody.put("details", fieldErrors);

        return new ResponseEntity<>(responseBody, HttpStatus.BAD_REQUEST);
    }
		// 702: 중복된 전화번호
		@ExceptionHandler(IllegalArgumentException.class)
		public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(IllegalArgumentException ex) {
			Map<String, Object> responseBody = new HashMap<>();
			String message = ex.getMessage();

			// 메시지에 "전화번호"가 포함된 경우 702, 그 외는 일반 400 오류 메시지 처리
			if (message.contains("전화번호")) {
				responseBody.put("code", 702);
				responseBody.put("message", message);
				return new ResponseEntity<>(responseBody, HttpStatus.CONFLICT); // 409 CONFLICT
			} else {
				// 로그인 실패 ("아이디 또는 비밀번호 불일치") 와 같은 기타 IllegalArgumentException 처리
				responseBody.put("code", 400);
				responseBody.put("message", message);
				return new ResponseEntity<>(responseBody, HttpStatus.BAD_REQUEST);
			}
		}

	// 703: CustomException 처리 (보호자-노인 매핑 오류 등 비즈니스 로직 오류)
	@ExceptionHandler(CustomException.class)
	public ResponseEntity<Map<String, Object>> handleCustomException(CustomException ex) {
		Map<String, Object> responseBody = new HashMap<>();
		responseBody.put("code", 703);   // 커스텀 비즈니스 로직 에러
		responseBody.put("message", ex.getMessage());
		return new ResponseEntity<>(responseBody, HttpStatus.BAD_REQUEST);
	}
}