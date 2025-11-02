package com.example.lowflightzone.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "airport_terminal_info")
@Getter
@Setter
public class AirportTerminalInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Связь с аэропортом (Many-to-One)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "airport_code", nullable = false)
    private Airport airport;

    @Column(name = "terminal", length = 5)
    private String terminal;

    @Enumerated(EnumType.STRING)
    @Column(name = "congestion_level")
    private CongestionLevel congestionLevel;

    @Column(name = "wait_time_minutes")
    private Integer waitTimeMinutes;

    @Column(name = "weather_conditions")
    private String weatherConditions;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    public enum CongestionLevel {
        LOW, MEDIUM, HIGH, VERY_HIGH
    }

    @PreUpdate
    protected void onUpdate() {
        lastUpdated = LocalDateTime.now();
    }
}