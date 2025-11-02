package com.example.lowflightzone.dao;

import com.example.lowflightzone.entity.FlightSubscription;
import com.example.lowflightzone.repositories.FlightSubscriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class FlightSubscriptionDao {

    private final FlightSubscriptionRepository subscriptionRepository;

    @Autowired
    public FlightSubscriptionDao(FlightSubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    public List<FlightSubscription> findActiveByUserId(Integer userId) {
        return subscriptionRepository.findActiveSubscriptionsByUserId(userId);
    }

    public List<FlightSubscription> findAll() {
        return subscriptionRepository.findAll();
    }

    public Optional<FlightSubscription> findActiveByUserEmailAndFlightNumber(String userEmail, String flightNumber) {
        return subscriptionRepository.findActiveByUserEmailAndFlightNumber(userEmail, flightNumber);
    }

    public FlightSubscription save(FlightSubscription subscription) {
        return subscriptionRepository.save(subscription);
    }

    public List<FlightSubscription> findByFlightNumberAndStatus(
            String flightNumber,
            FlightSubscription.SubscriptionStatus status
    ) {
        return subscriptionRepository.findByFlightNumberAndStatus(flightNumber, status);
    }

    public List<FlightSubscription> findByUserEmail(String userEmail) {
        return subscriptionRepository.findByUserEmailAndStatus(
                userEmail,
                FlightSubscription.SubscriptionStatus.ACTIVE
        );
    }

    public boolean existsByFlightAndUser(String flightNumber, String userEmail) {
        return subscriptionRepository.existsByFlight_FlightNumberAndUser_Email(flightNumber, userEmail);
    }

    // ✅ Новый: получить последнюю подписку (любой статус)
    public Optional<FlightSubscription> findLatestByFlightNumberAndUserEmail(String flightNumber, String email) {
        return subscriptionRepository.findFirstByFlight_FlightNumberAndUser_EmailOrderByIdDesc(flightNumber, email);
    }

    public void deleteById(Integer id) {
        subscriptionRepository.deleteById(id);
    }

    public boolean existsActiveByFlightAndUser(String flightNumber, String userEmail) {
        return subscriptionRepository.existsByFlight_FlightNumberAndUser_EmailAndStatus(
                flightNumber,
                userEmail,
                FlightSubscription.SubscriptionStatus.ACTIVE
        );
    }

}
