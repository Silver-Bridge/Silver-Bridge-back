package com.silverbridge.backend.service.calendar;

import com.silverbridge.backend.domain.User;
import com.silverbridge.backend.domain.calendar.CalendarEvent;
import com.silverbridge.backend.dto.calendar.CalendarDtos.*;
import com.silverbridge.backend.repository.UserRepository;
import com.silverbridge.backend.repository.calendar.CalendarEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CalendarServiceImpl implements CalendarService {

	private final CalendarEventRepository eventRepo;
	private final UserRepository userRepo;

	// 특정 월의 일정 조회
	@Override
	public List<CalendarDateItem> getCalendarDates(Long elderId, int year, int month) {

		LocalDate start = LocalDate.of(year, month, 1);
		LocalDate end = start.plusMonths(1).minusDays(1);

		return eventRepo.findByUserIdAndStartAtBetween(
						elderId,
						start.atStartOfDay(),
						end.atTime(23, 59))
				.stream()
				.map(e -> CalendarDateItem.builder()
						.id(e.getId())
						.memberId(e.getUser().getId())
						.date(e.getStartAt().toLocalDate())
						.build())
				.collect(Collectors.toList());
	}

	// 특정 날짜 일정 조회
	@Override
	public List<ScheduleItem> getSchedules(Long elderId, LocalDate date) {

		return eventRepo.findByUserIdAndStartAtBetween(
						elderId,
						date.atStartOfDay(),
						date.atTime(23, 59))
				.stream()
				.map(this::toScheduleItem)
				.collect(Collectors.toList());
	}

	// 일정 생성
	@Override
	@Transactional
	public void addSchedule(Long elderId, CreateScheduleRequest req) {

		User user = userRepo.findById(elderId)
				.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

		CalendarEvent event = CalendarEvent.builder()
				.user(user)
				.title(req.getTitle())
				.description(req.getDescription())
				.startAt(req.getStartAt().toLocalDateTime())
				.endAt(req.getEndAt().toLocalDateTime())
				.allDay(req.getAllDay() != null ? req.getAllDay() : false)
				.location(req.getLocation())
				.repeatType(req.getRepeatType() != null ? req.getRepeatType() : CalendarEvent.RepeatType.NONE)
				.priority(req.getPriority() != null ? req.getPriority() : CalendarEvent.Priority.MEDIUM)
				.alarmTime(req.getAlarmTime() != null ? req.getAlarmTime().toLocalDateTime() : null)
				.build();

		eventRepo.save(event);
	}

	// 일정 수정
	@Override
	@Transactional
	public ScheduleItem updateSchedule(Long elderId, Long scheduleId, UpdateScheduleRequest req) {

		CalendarEvent e = eventRepo.findById(scheduleId)
				.orElseThrow(() -> new IllegalArgumentException("수정할 일정이 없습니다."));

		if (!e.getUser().getId().equals(elderId)) {
			throw new SecurityException("본인 일정만 수정할 수 있습니다.");
		}

		e.setTitle(req.getTitle());
		e.setDescription(req.getDescription());
		e.setStartAt(req.getStartAt().toLocalDateTime());
		e.setEndAt(req.getEndAt().toLocalDateTime());
		e.setAllDay(req.getAllDay());
		e.setLocation(req.getLocation());
		e.setRepeatType(req.getRepeatType());
		e.setPriority(req.getPriority());
		e.setAlarmTime(req.getAlarmTime() != null ? req.getAlarmTime().toLocalDateTime() : null);

		return toScheduleItem(eventRepo.save(e));
	}

	// 일정 삭제
	@Override
	@Transactional
	public void deleteSchedule(Long elderId, Long scheduleId) {

		CalendarEvent e = eventRepo.findById(scheduleId)
				.orElseThrow(() -> new IllegalArgumentException("삭제할 일정이 없습니다."));

		if (!e.getUser().getId().equals(elderId)) {
			throw new SecurityException("본인 일정만 삭제할 수 있습니다.");
		}

		eventRepo.delete(e);
	}

	private ScheduleItem toScheduleItem(CalendarEvent e) {
		return ScheduleItem.builder()
				.id(e.getId())
				.title(e.getTitle())
				.description(e.getDescription())
				.startAt(e.getStartAt().atOffset(java.time.ZoneOffset.ofHours(9)))
				.endAt(e.getEndAt().atOffset(java.time.ZoneOffset.ofHours(9)))
				.allDay(e.getAllDay())
				.location(e.getLocation())
				.repeatType(e.getRepeatType())
				.priority(e.getPriority())
				.alarmTime(e.getAlarmTime() != null ? e.getAlarmTime().atOffset(java.time.ZoneOffset.ofHours(9)) : null)
				.build();
	}
}

