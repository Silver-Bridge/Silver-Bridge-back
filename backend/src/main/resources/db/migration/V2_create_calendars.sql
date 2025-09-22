CREATE TABLE calendars (
                           id BIGINT AUTO_INCREMENT PRIMARY KEY,
                           user_id BIGINT NOT NULL,
                           date DATE NOT NULL,
                           CONSTRAINT fk_calendars_user FOREIGN KEY (user_id) REFERENCES users(id),
                           INDEX idx_calendars_user_date (user_id, date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
