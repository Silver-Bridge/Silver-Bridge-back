package com.silverbridge.backend.domain.calendar;

import com.silverbridge.backend.domain.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;


@Entity
@Table(name = "calendar_event")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CalendarEvent {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false) private String title;
    @Column(columnDefinition = "TEXT") private String description;
    @Column(name="start_at", nullable=false) private LocalDateTime startAt;
    @Column(name="end_at",   nullable=false) private LocalDateTime endAt;
    @Column(name="all_day") private Boolean allDay = false;
    private String location;

    @Column(name="created_at", updatable=false)
    private LocalDateTime createdAt;

    @Column(name="updated_at")
    private LocalDateTime updatedAt;

    @PrePersist public void onCreate(){ createdAt = LocalDateTime.now(); updatedAt = createdAt; }
    @PreUpdate  public void onUpdate(){ updatedAt = LocalDateTime.now(); }
}
