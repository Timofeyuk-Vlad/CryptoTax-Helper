package com.example.lowflightzone.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "flights")
@Getter
@Setter
public class Flight {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "flight_number", unique = true, nullable = false, length = 10)
    private String flightNumber;

    @Column(name = "airline", nullable = false, length = 50)
    private String airline;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "departure_airport", referencedColumnName = "iata_code", nullable = false)
    private Airport departureAirport;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "arrival_airport", referencedColumnName = "iata_code", nullable = false)
    private Airport arrivalAirport;

    @Column(name = "scheduled_departure")
    private LocalDateTime scheduledDeparture;

    @Column(name = "scheduled_arrival")
    private LocalDateTime scheduledArrival;

    @Column(name = "estimated_departure")
    private LocalDateTime estimatedDeparture;

    @Column(name = "estimated_arrival")
    private LocalDateTime estimatedArrival;

    @Column(name = "actual_departure")
    private LocalDateTime actualDeparture;

    @Column(name = "actual_arrival")
    private LocalDateTime actualArrival;

    @Column(name = "delay_minutes")
    private Integer delayMinutes;

    @Column(name = "terminal", length = 5)
    private String terminal;

    @Column(name = "gate", length = 10)
    private String gate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private FlightStatus status;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    @OneToMany(mappedBy = "flight", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<FlightSubscription> subscriptions = new ArrayList<>();

    public enum FlightStatus {
        SCHEDULED, DELAYED, CANCELLED,  ARRIVED, DEPARTED
    }

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        lastUpdated = LocalDateTime.now();
        if (status == null) {
            status = FlightStatus.SCHEDULED;
        }
        if (delayMinutes == null) {
            delayMinutes = 0;
        }
    }

    public int getSubscriptionCount() {
        return subscriptions != null ? subscriptions.size() : 0;
    }
}
