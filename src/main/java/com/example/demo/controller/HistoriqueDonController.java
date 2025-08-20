package com.example.demo.controller;

import com.example.demo.model.Don;
import com.example.demo.model.StatutDon;
import com.example.demo.repository.DonRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HistoriqueDonController {

    private final DonRepository donRepository;

    public HistoriqueDonController(DonRepository donRepository) {
        this.donRepository = donRepository;
    }

    @GetMapping("/admin/dons/historique")
    public String afficherHistoriqueDons(Model model) {
        // Récupère tous les dons acceptés ou refusés
        List<Don> donsHistoriques = donRepository.findByStatutIn(List.of(StatutDon.ACCEPTE, StatutDon.REFUSE));
        model.addAttribute("donsHistoriques", donsHistoriques);
        return "admin/historique-dons"; // correspond au nom du fichier HTML
    }
}
