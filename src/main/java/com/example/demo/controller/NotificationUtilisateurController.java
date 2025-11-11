package com.example.demo.controller;

import com.example.demo.model.NotificationUtilisateur;
import com.example.demo.model.Utilisateur;
import com.example.demo.service.NotificationUtilisateurService;
import com.example.demo.repository.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/notifications")
public class NotificationUtilisateurController {

    @Autowired
    private NotificationUtilisateurService notificationService;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    /**
     * Récupérer toutes les notifications non lues pour un utilisateur.
     * @param userId l'ID de l'utilisateur pour lequel on souhaite récupérer les notifications non lues
     * @return la liste des notifications non lues pour l'utilisateur
     */
    @GetMapping("/utilisateur/{userId}/non-lues")
    public ResponseEntity<List<NotificationUtilisateur>> getNotificationsNonLues(@PathVariable Long userId) {
        try {
            Utilisateur utilisateur = utilisateurRepository.findById(userId).orElse(null);
            if (utilisateur == null) {
                return ResponseEntity.notFound().build();
            }

            List<NotificationUtilisateur> notifications = notificationService.getNotificationsNonLues(utilisateur);
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Envoyer une notification à un utilisateur.
     * @param userId l'ID de l'utilisateur pour lequel on souhaite envoyer une notification
     * @param message le message de la notification à envoyer
     * @return une réponse HTTP indiquant si l'opération a réussi (200 OK) ou si la notification n'a pas été trouvée (404 NOT FOUND)
     */
    @PostMapping("/envoyer/{userId}")
    public ResponseEntity<String> envoyerNotification(@PathVariable Long userId, @RequestParam String message) {
        try {
            // Vérifier que l'utilisateur existe
            Utilisateur utilisateur = utilisateurRepository.findById(userId).orElse(null);
            if (utilisateur == null) {
                return ResponseEntity.notFound().build();
            }

            // Envoyer la notification
            notificationService.envoyerNotification(utilisateur, message);
            return ResponseEntity.ok("Notification envoyée.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur serveur");
        }
    }

    /**
     * Marquer une ou plusieurs notifications comme lues.
     * @param notificationIds liste des IDs des notifications à marquer comme lues
     * @return une réponse HTTP indiquant si l'opération a réussi (200 OK) ou si une erreur s'est produite
     */
    @PostMapping("/marquer-lues")
    public ResponseEntity<String> marquerCommeLues(@RequestBody List<Long> notificationIds) {
        try {
            List<NotificationUtilisateur> notifications = new ArrayList<>();
            for (Long id : notificationIds) {
                NotificationUtilisateur notif = notificationService.getNotificationById(id);
                if (notif != null) {
                    notifications.add(notif);
                }
            }

            if (!notifications.isEmpty()) {
                notificationService.marquerCommeLues(notifications);
                return ResponseEntity.ok("Notifications marquées comme lues.");
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur serveur");
        }
    }

}