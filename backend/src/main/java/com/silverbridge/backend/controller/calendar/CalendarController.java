package com.silverbridge.backend.controller.calendar;

import com.silverbridge.backend.domain.User;
import com.silverbridge.backend.dto.calendar.CalendarDtos.*;
import com.silverbridge.backend.service.UserService;
import com.silverbridge.backend.service.calendar.CalendarService;
import com.silverbridge.backend.service.calendar.ElderAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/calendar")
public class CalendarController {

	private final CalendarService calendarService;
	private final ElderAccessService elderAccessService;
	private final UserService userService;

	// 특정 월의 일정 유무 조회
	@GetMapping
	public ResponseEntity<?> getCalendar(
			@RequestParam int year,
			@RequestParam int month,
			Authentication authentication
	) {
		String phone = authentication.getName();
		User user = userService.findByPhoneNumber(phone);

		// 노인/보호자 판단 → Elder ID 자동 결정
		Long elderId = elderAccessService.getAccessibleElderId(user.getId());

		List<CalendarDateItem> items = calendarService.getCalendarDates(elderId, year, month);
		return ResponseEntity.ok(CalendarDateListResponse.builder().body(items).build());
	}

	// 특정 날짜 상세 조회
	@GetMapping("/schedules")
	public ResponseEntity<?> getSchedules(
			@RequestParam String date,
			Authentication authentication
	) {
		String phone = authentication.getName();
		User user = userService.findByPhoneNumber(phone);
		Long elderId = elderAccessService.getAccessibleElderId(user.getId());

		LocalDate d = LocalDate.parse(date);
		List<ScheduleItem> items = calendarService.getSchedules(elderId, d);
		return ResponseEntity.ok(ScheduleListResponse.builder().body(items).build());
	}

	// 일정 추가
	@PostMapping("/add")
	public ResponseEntity<?> addSchedule(
			@RequestBody CreateScheduleRequest req,
			Authentication authentication
	) {
		String phone = authentication.getName();
		User user = userService.findByPhoneNumber(phone);
		Long elderId = elderAccessService.getAccessibleElderId(user.getId());

		calendarService.addSchedule(elderId, req);
		return ResponseEntity.ok(SimpleMessageResponse.builder().code(200).message("일정 추가 성공").build());
	}

	// 일정 수정
	@PutMapping("/schedule/{scheduleId}")
	public ResponseEntity<?> updateSchedule(
			@PathVariable Long scheduleId,
			@RequestBody UpdateScheduleRequest req,
			Authentication authentication
	) {
		String phone = authentication.getName();
		User user = userService.findByPhoneNumber(phone);
		Long elderId = elderAccessService.getAccessibleElderId(user.getId());

		ScheduleItem updated = calendarService.updateSchedule(elderId, scheduleId, req);
		return ResponseEntity.ok(updated);
	}

	// 일정 삭제
	@DeleteMapping("/schedule/{scheduleId}")
	public ResponseEntity<?> deleteSchedule(
			@PathVariable Long scheduleId,
			Authentication authentication
	) {
		String phone = authentication.getName();
		User user = userService.findByPhoneNumber(phone);
		Long elderId = elderAccessService.getAccessibleElderId(user.getId());

		calendarService.deleteSchedule(elderId, scheduleId);
		return ResponseEntity.ok(SimpleMessageResponse.builder().code(200).message("일정 삭제 성공").build());
	}

    // 일정 완료 상태
    @PatchMapping("/schedule/{scheduleId}/complete")
    public ResponseEntity<?> toggleComplete(
            @PathVariable Long scheduleId,
            Authentication authentication
    ) {
        String phone = authentication.getName();
        User user = userService.findByPhoneNumber(phone);
        Long elderId = elderAccessService.getAccessibleElderId(user.getId());

        calendarService.toggleScheduleCompletion(elderId, scheduleId);

        return ResponseEntity.ok(SimpleMessageResponse.builder()
                .code(200)
                .message("일정 완료 상태가 변경되었습니다.")
                .build());
    }

    // 1분마다 호출될 알람 체크 API
    @GetMapping("/alarm/check")
    public ResponseEntity<?> checkAlarm(Authentication authentication) {
        System.out.println("👉 1. 알람 체크 API 호출됨");

        if (authentication == null) {
            System.out.println("❌ 2. 인증 객체가 NULL입니다. (토큰 없음)");
            return ResponseEntity.status(401).build();
        }

        String phone = authentication.getName();
        System.out.println("👉 3. 토큰 사용자 전화번호: " + phone);

        try {
            User user = userService.findByPhoneNumber(phone);
            if (user == null) {
                System.out.println("❌ 4. DB에서 유저를 못 찾음: " + phone);
                return ResponseEntity.badRequest().body("User not found");
            }

            System.out.println("👉 5. 유저 ID: " + user.getId() + " / 알림설정: " + user.getAlarmActive());

            // 서비스 호출
            List<ScheduleItem> alarms = calendarService.checkAlarm(user.getId());
            System.out.println("✅ 6. 알람 조회 성공. 개수: " + alarms.size());

            return ResponseEntity.ok(ScheduleListResponse.builder()
                    .body(alarms)
                    .build());

        } catch (Exception e) {
            System.out.println("❌ 7. 에러 발생 원인: " + e.getMessage());
            e.printStackTrace(); // 콘솔에 빨간 에러 줄을 띄워줍니다.
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }
}
