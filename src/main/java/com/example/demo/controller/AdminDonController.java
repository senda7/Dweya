package com.example.demo.controller;

import com.example.demo.model.Don;
import com.example.demo.model.StatutDon;
import com.example.demo.repository.DonRepository;
import com.example.demo.service.NotificationUtilisateurService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class AdminDonController {

    private final DonRepository donRepository;
    private final NotificationUtilisateurService notificationService;

    public AdminDonController(DonRepository donRepository, NotificationUtilisateurService notificationService) {
        this.donRepository = donRepository;
        this.notificationService = notificationService;
    }

    @GetMapping("/admin/dons/accepter/{id}")
    public String accepterDon(@PathVariable("id") Long id, Model model) {
        Don don = donRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Don introuvable : " + id));
        don.setStatut(StatutDon.ACCEPTE);
        donRepository.save(don);

        // CORRECTION : Utiliser l'utilisateur du don, pas du médicament
        notificationService.envoyerNotification(
                don.getUtilisateur(), // CORRIGÉ
                "Votre don #" + id + " a été accepté par la pharmacie ✅"
        );

        int unreadCount = notificationService.countNotificationsNonLues(don.getUtilisateur());
        model.addAttribute("unreadCount", unreadCount);

        return "redirect:/admin/dons";
    }

    @GetMapping("/admin/dons/refuser/{id}")
    public String refuserDon(@PathVariable("id") Long id, Model model) {
        Don don = donRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Don introuvable : " + id));
        don.setStatut(StatutDon.REFUSE);
        donRepository.save(don);

        // CORRECTION : Message différent pour le refus
        notificationService.envoyerNotification(
                don.getUtilisateur(), // CORRIGÉ
                "Votre don #" + id + " a été refusé par la pharmacie ❌"
        );

        int unreadCount = notificationService.countNotificationsNonLues(don.getUtilisateur());
        model.addAttribute("unreadCount", unreadCount);

        return "redirect:/admin/dons";
    }
}
