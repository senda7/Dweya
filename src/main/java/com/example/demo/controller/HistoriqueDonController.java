package com.example.demo.controller;
import jakarta.servlet.http.HttpSession;
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

    @GetMapping("/pharmacie/dons/historique")
    public String afficherHistoriqueDons(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";

        // Récupère tous les dons acceptés ou refusés pour la pharmacie connectée
        List<Don> donsHistoriques = donRepository.findByPharmacie_IdAndStatutIn(
                userId, List.of(StatutDon.ACCEPTE, StatutDon.REFUSE)
        );
        model.addAttribute("donsHistoriques", donsHistoriques);
        return "pharmacie/historique-dons";
    }

    @GetMapping("/mes-historique")
    public String afficherHistoriqueDonsUtilisateur(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";
        List<Don> donsHistoriques = donRepository.findByMedicament_Utilisateur_IdAndStatutIn(
                userId, List.of(StatutDon.ACCEPTE, StatutDon.REFUSE)
        );
        model.addAttribute("donsHistoriques", donsHistoriques);
        return "utilisateur/mes-historique";
    }

}
