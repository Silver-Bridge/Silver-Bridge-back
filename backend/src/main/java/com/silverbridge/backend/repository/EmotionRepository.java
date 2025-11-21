package com.silverbridge.backend.repository;

import com.silverbridge.backend.dto.EmotionCountProjection;
import com.silverbridge.backend.domain.chatbot.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface EmotionRepository extends JpaRepository<ChatMessage, Long> {

	@Query(value = """
        SELECT 
            cm.emotion AS emotion,
            COUNT(*) AS cnt
        FROM chat_message cm
        JOIN chat_session cs ON cm.session_id = cs.id
        WHERE cs.user_id = :userId
          AND DATE(cm.created_at) BETWEEN DATE(:startDate) AND DATE(:endDate)
        GROUP BY cm.emotion
        """, nativeQuery = true)
	List<EmotionCountProjection> getEmotionSummaryNative(
			@Param("userId") Long userId,
			@Param("startDate") LocalDateTime startDate,
			@Param("endDate") LocalDateTime endDate
	);

	@Query(value = """
		SELECT 
			cm.emotion AS emotion,
			COUNT(*) AS cnt
		FROM chat_message cm
		JOIN chat_session cs 
			ON cm.session_id = cs.id
		WHERE cs.user_id = :userId
		  AND cm.created_at BETWEEN :startOfDay AND :sixPM
		GROUP BY cm.emotion
		ORDER BY cnt DESC
		LIMIT 1
		""", nativeQuery = true)
	EmotionCountProjection getTodayTopEmotion(
			@Param("userId") Long userId,
			@Param("startOfDay") LocalDateTime startOfDay,
			@Param("sixPM") LocalDateTime sixPM
	);


}
