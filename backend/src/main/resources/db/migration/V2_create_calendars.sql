-- 이벤트
CREATE TABLE IF NOT EXISTS calendar_event (
                                              id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                              user_id BIGINT NOT NULL,
                                              title VARCHAR(255) NOT NULL,
    description TEXT,
    start_at DATETIME NOT NULL,
    end_at   DATETIME NOT NULL,
    all_day  BIT DEFAULT 0,
    location VARCHAR(255),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_event_user FOREIGN KEY (user_id) REFERENCES users(id)
    );

-- 반복 규칙(선택)
CREATE TABLE IF NOT EXISTS calendar_recurrence (
                                                   id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                                   event_id BIGINT NOT NULL,
                                                   rrule VARCHAR(512) NOT NULL, -- 예: FREQ=WEEKLY;BYDAY=MO,WE,FR
    CONSTRAINT fk_recur_event FOREIGN KEY (event_id) REFERENCES calendar_event(id) ON DELETE CASCADE
    );
