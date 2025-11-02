package com.example.lowflightzone.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "flight_subscriptions",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"flight_id", "user_id", "status"}
        )
)

@Getter
@Setter
public class FlightSubscription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Связь с рейсом (Many-to-One)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flight_id", nullable = false)
    private Flight flight;

    // Связь с пользователем (Many-to-One)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "notification_types")
    private String notificationTypes;

    @Column(name = "device_token")
    private String deviceToken;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private SubscriptionStatus status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "last_notified_at")
    private LocalDateTime lastNotifiedAt;

    @Column(name = "notify_before_hours")
    private Integer notifyBeforeHours;

    @Column(name = "min_delay_minutes")
    private Integer minDelayMinutes;

    public enum SubscriptionStatus {
        ACTIVE, PAUSED, CANCELLED
    }

    @Column(nullable = false, length = 500)
    private String endpoint;

    @Column(nullable = false, length = 255)
    private String p256dh;

    @Column(nullable = false, length = 255)
    private String auth;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = SubscriptionStatus.ACTIVE;
        }
        if (notifyBeforeHours == null) {
            notifyBeforeHours = 2;
        }
        if (minDelayMinutes == null) {
            minDelayMinutes = 15;
        }
        if (notificationTypes == null) {
            notificationTypes = "DELAY,CANCELLATION,STATUS_CHANGE";
        }
    }
}