package com.cryptotax.helper.repository;

import com.cryptotax.helper.entity.TaxProfile;
import com.cryptotax.helper.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TaxProfileRepository extends JpaRepository<TaxProfile, Long> {
    Optional<TaxProfile> findByUser(User user);
    Optional<TaxProfile> findByUserId(Long userId);
}