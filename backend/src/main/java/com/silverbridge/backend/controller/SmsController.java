package com.silverbridge.backend.controller;

import com.silverbridge.backend.service.SmsService;
import com.silverbridge.backend.service.SmsVerificationManager;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.*;

@RestController
@RequestMapping("/api/sms")
@RequiredArgsConstructor
public class SmsController {

	private final SmsService smsService;
	private final SmsVerificationManager verificationManager;

	private final Map<String, String> verificationStorage = new ConcurrentHashMap<>();
	private final Map<String, ScheduledFuture<?>> expirationTasks = new ConcurrentHashMap<>();
	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

	@PostMapping("/send")
	public ResponseEntity<?> sendCode(@RequestParam String phoneNumber) {
		String code = smsService.sendVerificationCode(phoneNumber);

		verificationStorage.put(phoneNumber, code);
		if (expirationTasks.containsKey(phoneNumber))
			expirationTasks.get(phoneNumber).cancel(true);

		ScheduledFuture<?> expirationTask = scheduler.schedule(() -> {
			verificationStorage.remove(phoneNumber);
			expirationTasks.remove(phoneNumber);
			System.out.println("인증번호 만료됨: " + phoneNumber);
		}, 5, TimeUnit.MINUTES);

		expirationTasks.put(phoneNumber, expirationTask);
		return ResponseEntity.ok("인증번호가 발송되었습니다. (유효시간: 5분)");
	}

	@PostMapping("/verify")
	public ResponseEntity<?> verifyCode(@RequestParam String phoneNumber, @RequestParam String code) {
		String storedCode = verificationStorage.get(phoneNumber);

		if (storedCode != null && storedCode.equals(code)) {
			verificationStorage.remove(phoneNumber);
			if (expirationTasks.containsKey(phoneNumber)) {
				expirationTasks.get(phoneNumber).cancel(true);
				expirationTasks.remove(phoneNumber);
			}

			// 인증 성공 시 verifiedNumbers에 등록
			verificationManager.markVerified(phoneNumber);
			return ResponseEntity.ok("인증 성공");
		} else {
			return ResponseEntity.badRequest().body("인증번호가 일치하지 않거나 만료되었습니다.");
		}
	}
}

