package com.example.demo.controller;

import com.example.demo.model.Don;
import com.example.demo.model.Utilisateur;
import com.example.demo.repository.DonRepository;
import com.example.demo.repository.UtilisateurRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class DemandeController {

    @Autowired
    private DonRepository donRepository;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    // --- Afficher la page des demandes ---
    @GetMapping("/demande")
    public String afficherDemandes(HttpSession session,
                                   Model model,
                                   @RequestParam(value = "code_demande", required = false) String codeDemande,
                                   @ModelAttribute("message") String message,
                                   @ModelAttribute("error") String error) {

        // Vérifier session
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        Utilisateur pharmacieConnectee = utilisateurRepository.findById(userId).orElse(null);
        if (pharmacieConnectee == null) {
            return "redirect:/login";
        }

        List<Don> demandes;

        if (codeDemande != null && !codeDemande.isEmpty()) {
            // Recherche par codeDemande pour cette pharmacie uniquement
            demandes = donRepository.findByPharmacieAndCodeDemande(pharmacieConnectee, codeDemande);
        } else {
            // Tous les dons avec codeDemande ≠ null et ≠ "0"
            demandes = donRepository.findByPharmacieAndCodeDemandeIsNotNull(pharmacieConnectee)
                    .stream()
                    .filter(d -> !"0".equals(d.getCodeDemande()))
                    .toList();
        }

        model.addAttribute("demande", demandes);

        // Injecter messages dans le model
        if (message != null && !message.isEmpty()) {
            model.addAttribute("message", message);
        }
        if (error != null && !error.isEmpty()) {
            model.addAttribute("error", error);
        }

        return "pharmacie/demande";
    }

    // --- Marquer un don comme reçu ---
    @GetMapping("/pharmacie/dons/recu/{id}")
    public String recevoirDon(@PathVariable Long id,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {

        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        Don don = donRepository.findById(id).orElse(null);
        if (don == null) {
            redirectAttributes.addFlashAttribute("error", "❌ Don introuvable");
            return "redirect:/demande";
        }

        // Modifier codeDemande à "0" pour cacher le don
        don.setCodeDemande("0");
        donRepository.save(don);

        redirectAttributes.addFlashAttribute("message", "✅ Don reçu avec succès !");
        return "redirect:/demande";
    }
}
