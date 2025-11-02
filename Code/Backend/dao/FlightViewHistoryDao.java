package com.example.lowflightzone.dao;

import com.example.lowflightzone.entity.FlightViewHistory;
import com.example.lowflightzone.repositories.FlightViewHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public class FlightViewHistoryDao {

    private final FlightViewHistoryRepository viewHistoryRepository;

    @Autowired
    public FlightViewHistoryDao(FlightViewHistoryRepository viewHistoryRepository) {
        this.viewHistoryRepository = viewHistoryRepository;
    }

    public FlightViewHistory saveOrUpdateView(FlightViewHistory viewHistory) {
        return viewHistoryRepository.save(viewHistory);
    }

    public Optional<FlightViewHistory> findByUserIdAndFlightId(Integer userId, Integer flightId) {
        return viewHistoryRepository.findByUserIdAndFlightId(userId, flightId);
    }

    public List<FlightViewHistory> getRecentViewsByUserId(Integer userId, int limit) {
        return viewHistoryRepository
                .findByUser_IdOrderByViewedAtDesc(userId, PageRequest.of(0, limit))
                .getContent();
    }

    public List<FlightViewHistory> getMostViewedByUserId(Integer userId, int limit) {
        return viewHistoryRepository
                .findByUser_IdOrderByViewCountDescViewedAtDesc(userId, PageRequest.of(0, limit))
                .getContent();
    }

    public List<FlightViewHistory> getUserViewHistory(Integer userId) {
        return viewHistoryRepository.findByUserIdOrderByViewedAtDesc(userId);
    }

    public boolean existsByUserIdAndFlightId(Integer userId, Integer flightId) {
        return viewHistoryRepository.existsByUserIdAndFlightId(userId, flightId);
    }

    public Integer getViewCount(Integer userId, Integer flightId) {
        return viewHistoryRepository.getViewCountByUserAndFlight(userId, flightId);
    }

    public void deleteViewHistory(Integer id) {
        viewHistoryRepository.deleteById(id);
    }

    public void deleteByUserAndFlight(Integer userId, Integer flightId) {
        viewHistoryRepository.findByUserIdAndFlightId(userId, flightId)
                .ifPresent(viewHistory -> viewHistoryRepository.deleteById(viewHistory.getId()));
    }
}