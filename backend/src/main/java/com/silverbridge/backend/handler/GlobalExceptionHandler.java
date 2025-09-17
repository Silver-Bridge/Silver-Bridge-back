/*
명세에 포함된 604 (닉네임), 608 (지역), 609 (가입유형) 코드는 요청 바디에 해당 필드가 없거나 DTO와 매핑되지 않기 때문에 처리 로직에서 제외함.
필요할 시, DTO에 해당 필드를 추가하고 유효성 검사 규칙을 정의할 것.
 */
package com.silverbridge.backend.handler;

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

        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            if (fieldName.equals("password")) responseBody.put("code", 601);
            else if (fieldName.equals("email")) responseBody.put("code", 602);
            else if (fieldName.equals("name")) responseBody.put("code", 603);
            else if (fieldName.equals("birth")) responseBody.put("code", 605);
            else if (fieldName.equals("gender")) responseBody.put("code", 606);
            else if (fieldName.equals("phoneNumber")) responseBody.put("code", 607);
        });

        return new ResponseEntity<>(responseBody, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(IllegalArgumentException ex) {
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("code", ex.getMessage().contains("이메일") ? 701 : 702);
        responseBody.put("message", ex.getMessage());
        return new ResponseEntity<>(responseBody, HttpStatus.CONFLICT);
    }
}