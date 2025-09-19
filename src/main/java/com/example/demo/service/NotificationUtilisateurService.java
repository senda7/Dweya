package com.example.demo.service;
import com.example.demo.model.NotificationUtilisateur;
import com.example.demo.model.Utilisateur;
import com.example.demo.repository.NotificationUtilisateurRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationUtilisateurService {
    private final NotificationUtilisateurRepository notificationRepo;

    public NotificationUtilisateurService(NotificationUtilisateurRepository notificationRepo) {
        this.notificationRepo = notificationRepo;
    }

    public void envoyerNotification(Utilisateur utilisateur, String message) {
        NotificationUtilisateur notif = new NotificationUtilisateur(message, utilisateur);
        notificationRepo.save(notif);
    }

    public List<NotificationUtilisateur> getNotificationsNonLues(Utilisateur utilisateur) {
        return notificationRepo.findByUtilisateurIdAndLuFalse(utilisateur.getId());
    }

    public int countNotificationsNonLues(Utilisateur utilisateur) {
        return notificationRepo.countByUtilisateurIdAndLuFalse(utilisateur.getId());
    }
    public void marquerCommeLues(List<NotificationUtilisateur> notifications) {
        if (notifications == null || notifications.isEmpty()) {
            throw new IllegalArgumentException("La liste de notifications ne peut pas être vide");
        }

        notifications.forEach(n -> n.setLu(true));
        notificationRepo.saveAll(notifications);
    }
    public NotificationUtilisateur getNotificationById(Long id) {
        return notificationRepo.findById(id).orElse(null);
    }
}