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

    // ✅ CORRECTION: Utilisez le bon nom de propriété (isRead ou read)
    List<Notification> findByPharmacieIdAndIsReadFalseOrderByCreatedAtDesc(Long pharmacieId);

    // ✅ CORRECTION: Même chose ici
    int countByPharmacieIdAndIsReadFalse(Long pharmacieId);

    // ✅ CORRECTION: Méthode simplifiée et corrigée
    @Query("SELECT COUNT(n) > 0 FROM Notification n " +
            "WHERE n.pharmacieId = :pharmacieId " +
            "AND n.type = 'STOCK_ALERT' " +
            "AND n.message LIKE %:medicamentName% " +
            "AND n.isRead = false")
    boolean existsUnreadStockAlertForMedicament(@Param("pharmacieId") Long pharmacieId,
                                                @Param("medicamentName") String medicamentName);
}