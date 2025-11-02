package com.example.lowflightzone.dao;

import com.example.lowflightzone.entity.Flight;
import com.example.lowflightzone.repositories.FlightRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public class FlightDao {

    private final FlightRepository flightRepository;

    @Autowired
    public FlightDao(FlightRepository flightRepository) {
        this.flightRepository = flightRepository;
    }

    public boolean existsByFlightNumber(String flightNumber) {
        return flightRepository.existsByFlightNumber(flightNumber);
    }

    public List<Flight> findAll() {
        return flightRepository.findAll();
    }

    public Optional<Flight> findById(Integer id) {
        return flightRepository.findById(id);
    }

    public Optional<Flight> findByFlightNumber(String flightNumber) {
        return flightRepository.findByFlightNumber(flightNumber);
    }

    public Flight save(Flight flight) {
        return flightRepository.save(flight);
    }

    @Transactional
    public void deleteById(Integer id) {
        flightRepository.deleteById(id);
    }
}
