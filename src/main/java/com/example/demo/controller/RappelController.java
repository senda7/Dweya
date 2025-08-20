package com.example.demo.controller;

import com.example.demo.model.Rappel;
import com.example.demo.model.Medicament;
import com.example.demo.repository.MedicamentRepository;
import com.example.demo.repository.RappelRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class RappelController {

    @Autowired
    private RappelRepository rappelRepository;

    @Autowired
    private MedicamentRepository medicamentRepository;

    //afficher rappels liés uniquement aux médicaments de l'utilisateur connecté
    @GetMapping("/rappels")
    public String listerRappels(Model model, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";

        List<Rappel> rappels = rappelRepository.findByMedicament_Utilisateur_Id(userId);
        model.addAttribute("rappels", rappels);
        return "utilisateur/rappels";
    }

    //afficher formulaire d'ajout
    @GetMapping("/ajouter-rappel")
    public String showForm(Model model, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";

        List<Medicament> medicaments = medicamentRepository.findByUtilisateurId(userId);
        model.addAttribute("rappel", new Rappel());
        model.addAttribute("medicaments", medicaments);
        return "utilisateur/ajouter-rappel";
    }

    //traiter ajout
    @PostMapping("/ajouter-rappel")
    public String submitForm(@ModelAttribute Rappel rappel, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";

        Medicament med = medicamentRepository.findById(rappel.getMedicament().getId()).orElse(null);
        if (med == null || !med.getUtilisateur().getId().equals(userId)) {
            return "redirect:/rappels";
        }

        rappel.setMedicament(med);
        rappelRepository.save(rappel);
        return "redirect:/rappels";
    }

    //formulaire de modification
    @GetMapping("/modifier-rappel/{id}")
    public String afficherFormulaireModification(@PathVariable Long id, Model model, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";

        Rappel rappel = rappelRepository.findById(id).orElse(null);
        if (rappel == null || !rappel.getMedicament().getUtilisateur().getId().equals(userId)) {
            return "redirect:/rappels";
        }

        List<Medicament> medicaments = medicamentRepository.findByUtilisateurId(userId);
        model.addAttribute("rappel", rappel);
        model.addAttribute("medicaments", medicaments);
        return "utilisateur/modifier-rappel";
    }

    //enregistrer modification
    @PostMapping("/modifier-rappel")
    public String modifierRappel(@ModelAttribute Rappel rappel, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";

        Medicament med = medicamentRepository.findById(rappel.getMedicament().getId()).orElse(null);
        if (med == null || !med.getUtilisateur().getId().equals(userId)) {
            return "redirect:/rappels";
        }

        rappel.setMedicament(med);
        rappelRepository.save(rappel);
        return "redirect:/rappels";
    }

    //suppression
    @GetMapping("/supprimer-rappel/{id}")
    public String supprimer(@PathVariable Long id, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";

        Rappel rappel = rappelRepository.findById(id).orElse(null);
        if (rappel != null && rappel.getMedicament().getUtilisateur().getId().equals(userId)) {
            rappelRepository.deleteById(id);
        }

        return "redirect:/rappels";
    }
}
