package com.example.demo.repository;

import com.example.demo.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByPharmacieIdOrderByCreatedAtDesc(Long pharmacieId);
    List<Notification> findByPharmacieIdAndIsReadFalseOrderByCreatedAtDesc(Long pharmacieId);
    int countByPharmacieIdAndIsReadFalse(Long pharmacieId);

    @Query("SELECT CASE WHEN COUNT(n) > 0 THEN true ELSE false END " +
            "FROM Notification n " +
            "WHERE n.pharmacieId = :pharmacieId AND n.type = 'STOCK_ALERT' " +
            "AND n.message = :message AND n.isRead = false")
    boolean existsUnreadStockAlert(@Param("pharmacieId") Long pharmacieId,
                                   @Param("message") String message);
}
