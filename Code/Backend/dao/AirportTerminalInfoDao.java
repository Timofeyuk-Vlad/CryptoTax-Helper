package com.example.lowflightzone.dao;

import com.example.lowflightzone.entity.AirportTerminalInfo;
import com.example.lowflightzone.repositories.AirportTerminalInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public class AirportTerminalInfoDao {

    private final AirportTerminalInfoRepository terminalInfoRepository;

    @Autowired
    public AirportTerminalInfoDao(AirportTerminalInfoRepository terminalInfoRepository) {
        this.terminalInfoRepository = terminalInfoRepository;
    }

    public List<AirportTerminalInfo> findAll() {
        return terminalInfoRepository.findAll();
    }

    public Optional<AirportTerminalInfo> findById(Integer id) {
        return terminalInfoRepository.findById(id);
    }

    public Optional<AirportTerminalInfo> findByAirportAndTerminal(String airportCode, String terminal) {
        return terminalInfoRepository.findByAirport_IataCodeAndTerminal(airportCode, terminal);
    }

    public List<AirportTerminalInfo> findByAirportCode(String airportCode) {
        return terminalInfoRepository.findByAirport_IataCode(airportCode);
    }

    public List<AirportTerminalInfo> findByCongestionLevel(AirportTerminalInfo.CongestionLevel... levels) {
        return terminalInfoRepository.findByCongestionLevelIn(List.of(levels));
    }

    public AirportTerminalInfo save(AirportTerminalInfo terminalInfo) {
        return terminalInfoRepository.save(terminalInfo);
    }

    public void deleteById(Integer id) {
        terminalInfoRepository.deleteById(id);
    }
}