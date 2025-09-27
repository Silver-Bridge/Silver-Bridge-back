CREATE TABLE schedules (
                           id BIGINT AUTO_INCREMENT PRIMARY KEY,
                           calendar_id BIGINT NOT NULL,
                           user_id BIGINT NOT NULL,
                           title VARCHAR(100) NOT NULL,
                           description VARCHAR(1000),
                           alarm_time DATETIME NOT NULL,
                           CONSTRAINT fk_schedules_calendar FOREIGN KEY (calendar_id) REFERENCES calendars(id),
                           CONSTRAINT fk_schedules_user FOREIGN KEY (user_id) REFERENCES users(id),
                           INDEX idx_schedules_calendar (calendar_id),
                           INDEX idx_schedules_user_alarm (user_id, alarm_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
