package com.cryptotax.helper.repository;

import com.cryptotax.helper.entity.Notification;
import com.cryptotax.helper.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUserOrderByCreatedAtDesc(User user);
    List<Notification> findByUserAndIsReadFalseOrderByCreatedAtDesc(User user);
    List<Notification> findByUserAndIsImportantTrueOrderByCreatedAtDesc(User user);

    @Query("SELECT n FROM Notification n WHERE n.user = :user AND n.createdAt >= :since ORDER BY n.createdAt DESC")
    List<Notification> findRecentByUser(@Param("user") User user, @Param("since") LocalDateTime since);

    long countByUserAndIsReadFalse(User user);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = CURRENT_TIMESTAMP WHERE n.user = :user AND n.isRead = false")
    void markAllAsReadByUser(@Param("user") User user);

    @Modifying
    @Query("DELETE FROM Notification n WHERE n.user = :user AND n.createdAt < :before")
    void deleteOldNotifications(@Param("user") User user, @Param("before") LocalDateTime before);
}