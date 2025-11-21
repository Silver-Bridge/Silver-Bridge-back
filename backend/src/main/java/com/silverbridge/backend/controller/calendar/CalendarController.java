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
}
