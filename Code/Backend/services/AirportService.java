package com.example.lowflightzone.services;

import com.example.lowflightzone.dao.AirportDao;
import com.example.lowflightzone.dto.AirportDto;
import com.example.lowflightzone.entity.Airport;
import com.example.lowflightzone.exceptions.AirportException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AirportService {

    private final AirportDao airportDao;

    @Autowired
    public AirportService(AirportDao airportDao) {
        this.airportDao = airportDao;
    }

    public List<AirportDto> getAllAirports() {
        return airportDao.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public AirportDto getAirportByCode(String iataCode) {
        Airport airport = airportDao.findByIataCode(iataCode)
                .orElseThrow(() -> new AirportException("Аэропорт не найден: " + iataCode));
        return convertToDto(airport);
    }

    public List<AirportDto> getAirportsByCity(String city) {
        // Убираем equalsIgnoreCase, используем обычный поиск
        return airportDao.findByCity(city).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public AirportDto addAirport(AirportDto airportDto) {
        if (airportDao.existsByIataCode(airportDto.getIataCode())) {
            throw new AirportException("Аэропорт с кодом " + airportDto.getIataCode() + " уже существует");
        }

        Airport airport = convertToEntity(airportDto);
        Airport savedAirport = airportDao.save(airport);
        return convertToDto(savedAirport);
    }

    private AirportDto convertToDto(Airport airport) {
        AirportDto dto = new AirportDto();
        dto.setIataCode(airport.getIataCode());
        dto.setName(airport.getName());
        dto.setCity(airport.getCity());
        dto.setCountry(airport.getCountry());
        dto.setLatitude(airport.getLatitude());
        dto.setLongitude(airport.getLongitude());
        dto.setTimezone(airport.getTimezone());
        dto.setAltitude(airport.getAltitude());
        return dto;
    }

    private Airport convertToEntity(AirportDto dto) {
        Airport airport = new Airport();
        airport.setIataCode(dto.getIataCode());
        airport.setName(dto.getName());
        airport.setCity(dto.getCity());
        airport.setCountry(dto.getCountry());
        airport.setLatitude(dto.getLatitude());
        airport.setLongitude(dto.getLongitude());
        airport.setTimezone(dto.getTimezone());
        airport.setAltitude(dto.getAltitude());
        return airport;
    }
}