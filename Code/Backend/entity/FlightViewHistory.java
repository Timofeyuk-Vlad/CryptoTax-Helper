package com.example.lowflightzone.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "flight_view_history",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "flight_id"}))
@Getter
@Setter
public class FlightViewHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Связь с пользователем
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Связь с рейсом
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "flight_id", nullable = false)
    private Flight flight;


    @Column(name = "viewed_at", nullable = false)
    private LocalDateTime viewedAt;

    @Column(name = "view_count")
    private Integer viewCount = 1;

    @PrePersist
    protected void onCreate() {
        viewedAt = LocalDateTime.now();
        if (viewCount == null) {
            viewCount = 1;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        if (viewCount == null) {
            viewCount = 1;
        }
    }
}