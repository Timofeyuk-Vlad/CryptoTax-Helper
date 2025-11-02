package com.example.lowflightzone.repositories;

import com.example.lowflightzone.entity.FlightSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FlightSubscriptionRepository extends JpaRepository<FlightSubscription, Integer> {

    List<FlightSubscription> findByUserEmailAndStatus(
            String userEmail,
            FlightSubscription.SubscriptionStatus status
    );

    List<FlightSubscription> findByStatus(FlightSubscription.SubscriptionStatus status);

    // Поиск подписок на конкретный рейс по его номеру и статусу
    @Query("SELECT fs FROM FlightSubscription fs " +
            "WHERE fs.flight.flightNumber = :flightNumber AND fs.status = :status")
    List<FlightSubscription> findByFlightNumberAndStatus(
            String flightNumber,
            FlightSubscription.SubscriptionStatus status
    );

    @Query("SELECT fs FROM FlightSubscription fs " +
            "WHERE fs.user.email = :userEmail " +
            "AND fs.flight.flightNumber = :flightNumber " +
            "AND fs.status = 'ACTIVE'")
    Optional<FlightSubscription> findActiveByUserEmailAndFlightNumber(
            @Param("userEmail") String userEmail,
            @Param("flightNumber") String flightNumber
    );

    // ✅ Новый метод: находим последнюю подписку по user + flight (любой статус)
    Optional<FlightSubscription> findFirstByFlight_FlightNumberAndUser_EmailOrderByIdDesc(
            String flightNumber,
            String email
    );

    boolean existsByFlight_FlightNumberAndUser_EmailAndStatus(
            String flightNumber,
            String userEmail,
            FlightSubscription.SubscriptionStatus status
    );

    Optional<FlightSubscription> findFirstByUserIdAndStatus(Integer userId, FlightSubscription.SubscriptionStatus status);

    // Проверка существования подписки у пользователя на рейс
    boolean existsByFlight_FlightNumberAndUser_Email(String flightNumber, String userEmail);



    // Активные подписки пользователя
    @Query("SELECT fs FROM FlightSubscription fs WHERE fs.user.id = :userId AND fs.status = 'ACTIVE'")
    List<FlightSubscription> findActiveSubscriptionsByUserId(Integer userId);

    // 🔎 Найти активную подписку по email пользователя и номеру рейса
    Optional<FlightSubscription> findByUser_EmailAndFlight_FlightNumberAndStatus(
            String userEmail,
            String flightNumber,
            FlightSubscription.SubscriptionStatus status
    );

    List<FlightSubscription> findAllByFlight_Id(Integer flightId);

    List<FlightSubscription> findAllByFlightIdAndStatus(Integer flightId, FlightSubscription.SubscriptionStatus status);


}
