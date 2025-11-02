package com.example.lowflightzone.dao;

import com.example.lowflightzone.entity.Airport;
import com.example.lowflightzone.repositories.AirportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public class AirportDao {

    private final AirportRepository airportRepository;

    @Autowired
    public AirportDao(AirportRepository airportRepository) {
        this.airportRepository = airportRepository;
    }

    public List<Airport> findAll() {
        return airportRepository.findAll();
    }

    public Optional<Airport> findByIataCode(String iataCode) {
        return airportRepository.findByIataCode(iataCode);
    }

    public List<Airport> findByCity(String city) {
        return airportRepository.findByCityContainingIgnoreCase(city);
    }

    public Airport save(Airport airport) {
        return airportRepository.save(airport);
    }

    public boolean existsByIataCode(String iataCode) {
        return airportRepository.existsByIataCode(iataCode);
    }

    public void deleteByIataCode(String iataCode) {
        airportRepository.deleteById(iataCode);
    }
}