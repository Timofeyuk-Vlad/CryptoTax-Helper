package com.cryptotax.helper.repository;

import com.cryptotax.helper.entity.Transaction;
import com.cryptotax.helper.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByUserOrderByTimestampDesc(User user);
    List<Transaction> findByUserAndTimestampBetweenOrderByTimestampDesc(User user, LocalDateTime start, LocalDateTime end);

    @Query("SELECT t FROM Transaction t WHERE t.user = :user AND YEAR(t.timestamp) = :year ORDER BY t.timestamp DESC")
    List<Transaction> findByUserAndYearOrderByTimestampDesc(@Param("user") User user, @Param("year") int year);

    boolean existsByUserAndExchangeTxId(User user, String exchangeTxId);

    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.user = :user")
    long countByUser(@Param("user") User user);
}