package com.example.demo.controller;

import com.example.demo.model.Don;
import com.example.demo.model.StatutDon;
import com.example.demo.repository.DonRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
public class PharmacieDonController {

    private final DonRepository donRepository;

    public PharmacieDonController(DonRepository donRepository) {
        this.donRepository = donRepository;
    }

    // Page Espace des Dons de pharma
    @GetMapping("/dons-pharmacie")
    public String afficherEspaceDons(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";

        return "pharmacie/dons-pharmacie";
    }

    // Page Demandes des Dons (uniquement les dons en cours pour la pharmacie connectée)
    @GetMapping("/demandes-dons")
    public String afficherDemandesDons(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";

        // Récupérer uniquement les dons en cours liés à la pharmacie
        List<Don> demandesEnCours = donRepository.findByPharmacieIdAndStatut(userId, StatutDon.EN_COURS);
        model.addAttribute("tousLesDons", demandesEnCours);
        return "pharmacie/demandes-dons";
    }

    // Accepter un don
    @GetMapping("/pharmacie/dons/accepter/{id}")
    public String accepterDon(@PathVariable("id") Long id) {
        Don don = donRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Don introuvable : " + id));
        don.setStatut(StatutDon.ACCEPTE);
        donRepository.save(don);
        return "redirect:/demandes-dons";
    }

    // Refuser un don
    @GetMapping("/pharmacie/dons/refuser/{id}")
    public String refuserDon(@PathVariable("id") Long id) {
        Don don = donRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Don introuvable : " + id));
        don.setStatut(StatutDon.REFUSE);
        donRepository.save(don);
        return "redirect:/demandes-dons";
    }

}
