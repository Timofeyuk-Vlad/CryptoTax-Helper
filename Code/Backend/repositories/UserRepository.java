package com.example.lowflightzone.repositories;

import com.example.lowflightzone.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<User> findByLastName(String lastName);

    boolean existsByLastName(String lastName);

    @Query("SELECT u FROM User u JOIN u.subscriptions s WHERE s.flight.flightNumber = :flightNumber")
    List<User> findUsersSubscribedToFlight(@Param("flightNumber") String flightNumber);
}