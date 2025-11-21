package com.silverbridge.backend.dto;

import com.silverbridge.backend.domain.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserResponse {

	private Long id;
	private String name;
	private String phoneNumber;
	private String birth;
	private Boolean gender;
	private String region;
	private String textsize;
	private Boolean social;
	private String role;
	private Long connectedElderId;
	private Boolean alarmActive;


	public static UserResponse from(User user) {
		return UserResponse.builder()
				.id(user.getId())
				.name(user.getName())
				.phoneNumber(user.getPhoneNumber())
				.birth(user.getBirth())
				.gender(user.getGender())
				.region(user.getRegion())
				.textsize(user.getTextsize())
				.social(user.getSocial())
				.role(user.getRole())
				.connectedElderId(user.getConnectedElderId())
				.alarmActive(user.getAlarmActive())
				.build();
	}
}