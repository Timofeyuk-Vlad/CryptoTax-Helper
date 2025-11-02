package com.example.lowflightzone.repositories;

import com.example.lowflightzone.entity.AirportTerminalInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AirportTerminalInfoRepository extends JpaRepository<AirportTerminalInfo, Integer> {
    Optional<AirportTerminalInfo> findByAirport_IataCodeAndTerminal(String airportCode, String terminal);
    List<AirportTerminalInfo> findByAirport_IataCode(String airportCode);

    // Добавляем метод для поиска по нескольким уровням загруженности
    List<AirportTerminalInfo> findByCongestionLevelIn(List<AirportTerminalInfo.CongestionLevel> levels);
}