package com.example.demo.controller;

import com.example.demo.model.Don;
import com.example.demo.model.StatutDon;
import com.example.demo.repository.DonRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
public class AdminDonController {

    private final DonRepository donRepository;

    public AdminDonController(DonRepository donRepository) {
        this.donRepository = donRepository;
    }

    // Afficher tous les dons en cours pour l'admin
    @GetMapping("/admin/dons")
    public String listeDonsAdmin(Model model) {
        List<Don> donsEnCours = donRepository.findByStatut(StatutDon.EN_COURS);
        model.addAttribute("tousLesDons", donsEnCours);
        return "admin/dons-admin"; // nom de ton fichier Thymeleaf
    }

    // Accepter un don
    @GetMapping("/admin/dons/accepter/{id}")
    public String accepterDon(@PathVariable("id") Long id) {
        Don don = donRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Don introuvable : " + id));
        don.setStatut(StatutDon.ACCEPTE);
        donRepository.save(don);
        return "redirect:/admin/dons";
    }

    // Refuser un don
    @GetMapping("/admin/dons/refuser/{id}")
    public String refuserDon(@PathVariable("id") Long id) {
        Don don = donRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Don introuvable : " + id));
        don.setStatut(StatutDon.REFUSE);
        donRepository.save(don);
        return "redirect:/admin/dons";
    }

}
