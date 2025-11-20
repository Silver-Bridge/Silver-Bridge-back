package com.silverbridge.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EmotionCountDto {
	private String emotion;
	private Long count;
}
