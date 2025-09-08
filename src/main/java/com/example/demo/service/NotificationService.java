package com.example.demo.service;

import com.example.demo.model.Medpharmacie;
import com.example.demo.model.Notification;
import com.example.demo.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    public boolean createStockAlert(Medpharmacie medicament, Long pharmacieId) {
        try {
            // Vérifier si le médicament est vraiment en rupture
            if (medicament.getQuantite() > 0) {
                return false;
            }

            String message = "⚠️ Rupture de stock: " + medicament.getNom() + " est épuisé.";

            boolean notificationExists = notificationRepository
                    .existsByMessageContainingAndPharmacieIdAndIsReadFalse(
                            medicament.getNom(), // Plus spécifique
                            pharmacieId
                    );

            if (!notificationExists) {
                Notification notification = new Notification();
                notification.setPharmacieId(pharmacieId);
                notification.setMessage(message);
                notification.setType("STOCK_ALERT");
                notification.setRead(false);
                notification.setCreatedAt(LocalDateTime.now());
                notificationRepository.save(notification);
                return true;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Notification> getUnreadNotifications(Long pharmacieId) {
        return notificationRepository.findByPharmacieIdAndIsReadFalseOrderByCreatedAtDesc(pharmacieId);
    }

    public int getUnreadCount(Long pharmacieId) {
        return notificationRepository.countByPharmacieIdAndIsReadFalse(pharmacieId);
    }

    public void markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            notification.setRead(true);
            notificationRepository.save(notification);
        });
    }

    public List<Notification> getAllNotifications(Long pharmacieId) {
        return notificationRepository.findByPharmacieIdOrderByCreatedAtDesc(pharmacieId);
    }
}