package com.cryptotax.helper.repository;

import com.cryptotax.helper.entity.Exchange;
import com.cryptotax.helper.entity.ExchangeConnection;
import com.cryptotax.helper.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExchangeConnectionRepository extends JpaRepository<ExchangeConnection, Long> {
    List<ExchangeConnection> findByUser(User user);
    List<ExchangeConnection> findByUserAndIsActiveTrue(User user);
    Optional<ExchangeConnection> findByUserAndExchange(User user, Exchange exchange);
    boolean existsByUserAndExchange(User user, Exchange exchange);
}