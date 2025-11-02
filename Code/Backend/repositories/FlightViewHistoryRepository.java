package com.example.lowflightzone.repositories;

import com.example.lowflightzone.entity.Flight;
import com.example.lowflightzone.entity.FlightViewHistory;
import com.example.lowflightzone.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface FlightViewHistoryRepository extends JpaRepository<FlightViewHistory, Integer> {

    // Найти запись по пользователю и рейсу
    Optional<FlightViewHistory> findByUserIdAndFlightId(Integer userId, Integer flightId);

    // Получить историю просмотров пользователя с пагинацией
    @Query("SELECT fvh FROM FlightViewHistory fvh WHERE fvh.user.id = :userId ORDER BY fvh.viewedAt DESC")
    List<FlightViewHistory> findByUserIdOrderByViewedAtDesc(@Param("userId") Integer userId);

    Page<FlightViewHistory> findByUser_IdOrderByViewedAtDesc(Integer userId, Pageable pageable);

    Optional<FlightViewHistory> findTopByFlight_IdAndUser_EmailOrderByViewedAtDesc(Integer flightId, String email);

    Optional<FlightViewHistory> findByUserAndFlight(User user, Flight flight);

    Page<FlightViewHistory> findByUser_IdOrderByViewCountDescViewedAtDesc(Integer userId, Pageable pageable);
    // Проверить, существует ли запись
    boolean existsByUserIdAndFlightId(Integer userId, Integer flightId);

    // Количество просмотров рейса пользователем
    @Query("SELECT COALESCE(SUM(fvh.viewCount), 0) FROM FlightViewHistory fvh WHERE fvh.user.id = :userId AND fvh.flight.id = :flightId")
    Integer getViewCountByUserAndFlight(@Param("userId") Integer userId, @Param("flightId") Integer flightId);
}