package com.example.lowflightzone.services;

import com.example.lowflightzone.dao.AirportDao;
import com.example.lowflightzone.dao.AirportTerminalInfoDao;
import com.example.lowflightzone.dto.TerminalInfoDto;
import com.example.lowflightzone.entity.Airport;
import com.example.lowflightzone.entity.AirportTerminalInfo;
import com.example.lowflightzone.exceptions.AirportException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AirportTerminalService {

    private final AirportTerminalInfoDao terminalInfoDao;
    private final AirportDao airportDao;

    @Autowired
    public AirportTerminalService(AirportTerminalInfoDao terminalInfoDao, AirportDao airportDao) {
        this.terminalInfoDao = terminalInfoDao;
        this.airportDao = airportDao;
    }

    public TerminalInfoDto getTerminalInfo(String airportCode, String terminal) {
        AirportTerminalInfo info = terminalInfoDao.findByAirportAndTerminal(airportCode, terminal)
                .orElseThrow(() -> new AirportException("Информация по терминалу не найдена"));
        return convertToDto(info);
    }

    public List<TerminalInfoDto> getAirportTerminals(String airportCode) {
        return terminalInfoDao.findByAirportCode(airportCode).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public TerminalInfoDto updateTerminalInfo(String airportCode, String terminal,
                                              TerminalInfoDto infoDto) {
        Airport airport = airportDao.findByIataCode(airportCode)
                .orElseThrow(() -> new AirportException("Аэропорт не найден: " + airportCode));

        AirportTerminalInfo terminalInfo = terminalInfoDao.findByAirportAndTerminal(airportCode, terminal)
                .orElse(new AirportTerminalInfo());

        terminalInfo.setAirport(airport);
        terminalInfo.setTerminal(terminal);
        terminalInfo.setCongestionLevel(AirportTerminalInfo.CongestionLevel.valueOf(
                infoDto.getCongestionLevel()));
        terminalInfo.setWaitTimeMinutes(infoDto.getWaitTimeMinutes());
        terminalInfo.setWeatherConditions(infoDto.getWeatherConditions());

        AirportTerminalInfo savedInfo = terminalInfoDao.save(terminalInfo);
        return convertToDto(savedInfo);
    }

    public List<TerminalInfoDto> getHighCongestionTerminals() {
        return terminalInfoDao.findByCongestionLevel(
                        AirportTerminalInfo.CongestionLevel.HIGH,
                        AirportTerminalInfo.CongestionLevel.VERY_HIGH).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private TerminalInfoDto convertToDto(AirportTerminalInfo info) {
        TerminalInfoDto dto = new TerminalInfoDto();
        dto.setId(info.getId());
        // Здесь нужно преобразовать Airport в AirportDto
        dto.setTerminal(info.getTerminal());
        dto.setCongestionLevel(info.getCongestionLevel().toString());
        dto.setWaitTimeMinutes(info.getWaitTimeMinutes());
        dto.setWeatherConditions(info.getWeatherConditions());
        dto.setLastUpdated(info.getLastUpdated());
        return dto;
    }
}