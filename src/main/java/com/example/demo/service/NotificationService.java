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

    // ✅ CORRECTION: Méthode simplifiée et sécurisée
    public boolean createStockAlert(Medpharmacie medicament, Long pharmacieId) {
        try {
            // Vérifications de sécurité
            if (medicament == null || medicament.getNom() == null) {
                System.out.println("❌ Médicament null ou sans nom");
                return false;
            }

            if (medicament.getQuantite() == null || medicament.getQuantite() > 0) {
                return false; // Pas en rupture
            }

            String message = "⚠️ Rupture de stock: " + medicament.getNom() + " est épuisé.";
            System.out.println("🔄 Tentative création alerte: " + message);

            // ✅ CORRECTION: Utilisation de la nouvelle méthode
            boolean exists = notificationRepository.existsUnreadStockAlertForMedicament(
                    pharmacieId, medicament.getNom());

            if (!exists) {
                Notification notif = new Notification();
                notif.setPharmacieId(pharmacieId);
                notif.setMessage(message);
                notif.setType("STOCK_ALERT");
                notif.setRead(false); // ✅ Assurez-vous que c'est setRead() et non setIsRead()
                notif.setCreatedAt(LocalDateTime.now());

                notificationRepository.save(notif);
                System.out.println("✅ Alerte créée: " + message);
                return true;
            }

            System.out.println("ℹ️ Alerte existe déjà pour: " + medicament.getNom());
            return false;

        } catch (Exception e) {
            System.out.println("💥 ERREUR createStockAlert: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // ✅ CORRECTION: Méthodes adaptées aux nouveaux noms
    public List<Notification> getUnreadNotifications(Long pharmacieId) {
        return notificationRepository.findByPharmacieIdAndIsReadFalseOrderByCreatedAtDesc(pharmacieId);
    }

    public int getUnreadCount(Long pharmacieId) {
        return notificationRepository.countByPharmacieIdAndIsReadFalse(pharmacieId);
    }

    public void markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            notification.setRead(true); // ✅ Assurez-vous que c'est setRead()
            notificationRepository.save(notification);
        });
    }

    public List<Notification> getAllNotifications(Long pharmacieId) {
        return notificationRepository.findByPharmacieIdOrderByCreatedAtDesc(pharmacieId);
    }

    // ✅ CORRECTION: Méthode améliorée
    public void checkAllMedicamentsForStockAlerts(List<Medpharmacie> medicaments, Long pharmacieId) {
        if (medicaments == null) return;

        int alertesCrees = 0;
        for (Medpharmacie med : medicaments) {
            if (med != null && med.getQuantite() != null && med.getQuantite() == 0) {
                boolean created = createStockAlert(med, pharmacieId);
                if (created) alertesCrees++;
            }
        }
        System.out.println("📊 Alertes créées: " + alertesCrees);
    }
}