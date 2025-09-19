package com.example.demo.controller;

import com.example.demo.model.Don;
import com.example.demo.model.StatutDon;
import com.example.demo.model.Utilisateur;
import com.example.demo.repository.DonRepository;
import com.example.demo.repository.UtilisateurRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
public class PharmacieDonController {

    private final DonRepository donRepository;
    private final UtilisateurRepository utilisateurRepository;

    public PharmacieDonController(DonRepository donRepository, UtilisateurRepository utilisateurRepository) {
        this.donRepository = donRepository;
        this.utilisateurRepository = utilisateurRepository;
    }

    // Page Espace des Dons de pharma
    @GetMapping("/dons-pharmacie")
    public String afficherEspaceDons(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";

        // Récupérer l'utilisateur connecté
        Utilisateur utilisateur = utilisateurRepository.findById(userId).orElse(null);
        if (utilisateur != null) {
            model.addAttribute("utilisateur", utilisateur);
        }

        return "pharmacie/dons-pharmacie";
    }

    // Page Demandes des Dons
    @GetMapping("/demandes-dons")
    public String afficherDemandesDons(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";

        // Récupérer l'utilisateur connecté
        Utilisateur utilisateur = utilisateurRepository.findById(userId).orElse(null);
        if (utilisateur != null) {
            model.addAttribute("utilisateur", utilisateur);
        }

        List<Don> demandesEnCours = donRepository.findByPharmacieIdAndStatut(userId, StatutDon.EN_COURS);
        model.addAttribute("tousLesDons", demandesEnCours);
        return "pharmacie/demandes-dons";
    }

    // Accepter un don
    @GetMapping("/pharmacie/dons/accepter/{id}")
    public String accepterDon(@PathVariable("id") Long id, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";

        Don don = donRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Don introuvable : " + id));

        if (don.getPharmacie().getId().equals(userId)) {
            don.setStatut(StatutDon.ACCEPTE);
            donRepository.save(don);
        }

        return "redirect:/demandes-dons";
    }

    // Refuser un don
    @GetMapping("/pharmacie/dons/refuser/{id}")
    public String refuserDon(@PathVariable("id") Long id, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";

        Don don = donRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Don introuvable : " + id));

        if (don.getPharmacie().getId().equals(userId)) {
            don.setStatut(StatutDon.REFUSE);
            donRepository.save(don);
        }

        return "redirect:/demandes-dons";
    }
}