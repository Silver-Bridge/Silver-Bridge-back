package com.silverbridge.backend.service;

import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.request.SingleMessageSendingRequest;
import net.nurigo.sdk.message.response.SingleMessageSentResponse;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class SmsService {

	private final DefaultMessageService messageService;

	@Value("${sms.sender}")
	private String senderNumber;

	public SmsService(
			@Value("${sms.api-key}") String apiKey,
			@Value("${sms.api-secret}") String apiSecret) {
		this.messageService = NurigoApp.INSTANCE.initialize(apiKey, apiSecret, "https://api.coolsms.co.kr");
	}

	// 인증번호 전송 메서드
	public String sendVerificationCode(String phoneNumber) {
		String verificationCode = generateCode();
		// System.out.println("발송된 인증번호(test용): " + verificationCode); // TODO: 테스트 완료 후 삭제

		Message message = new Message();
		message.setFrom(senderNumber);
		message.setTo(phoneNumber);
		message.setText("[SilverBridge] 인증번호는 [" + verificationCode + "] 입니다.");

		try {
			SingleMessageSendingRequest request = new SingleMessageSendingRequest(message);
			SingleMessageSentResponse response = messageService.sendOne(request);

			System.out.println("CoolSMS 응답: " + response);
		} catch (Exception e) {
			throw new RuntimeException("문자 전송 실패: " + e.getMessage(), e);
		}

		return verificationCode;
	}

	private String generateCode() {
		Random random = new Random();
		int code = 100000 + random.nextInt(900000); // 6자리 숫자
		return String.valueOf(code);
	}
}
