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

    // Crée une alerte stock si nécessaire
    public boolean createStockAlert(Medpharmacie medicament, Long pharmacieId) {
        try {
            if (medicament.getQuantite() == null || medicament.getQuantite() > 0) {
                return false; // pas en rupture
            }

            String message = "⚠️ Rupture de stock: " + medicament.getNom() + " est épuisé.";

            boolean exists = notificationRepository.existsUnreadStockAlert(pharmacieId, message);

            if (!exists) {
                Notification notif = new Notification();
                notif.setPharmacieId(pharmacieId);
                notif.setMessage(message);
                notif.setType("STOCK_ALERT");
                notif.setRead(false);
                notif.setCreatedAt(LocalDateTime.now());
                notificationRepository.save(notif);
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

    public void checkAllMedicamentsForStockAlerts(List<Medpharmacie> medicaments, Long pharmacieId) {
        for (Medpharmacie med : medicaments) {
            if (med.getQuantite() != null && med.getQuantite() == 0) {
                createStockAlert(med, pharmacieId);
            }
        }
    }
}
