package com.example.demo.controller;

import com.example.demo.model.Medicament;
import com.example.demo.model.Utilisateur;
import com.example.demo.repository.MedicamentRepository;
import com.example.demo.repository.UtilisateurRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class MedicamentController {

    @Autowired
    private MedicamentRepository medicamentRepo;

    @Autowired
    private UtilisateurRepository utilisateurRepo;

    // afficher tous les médicaments du user connecté
    @GetMapping("/medicament")
    public String afficherMesMedicaments(Model model, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        List<Medicament> medicaments = medicamentRepo.findByUtilisateurId(userId);
        model.addAttribute("medicaments", medicaments);
        return "utilisateur/medicament"; // Thymeleaf page medicament.html
    }

    //afficher formulaire d'ajout
    @GetMapping("/ajouter/medicament")
    public String formAjout(Model model, HttpSession session) {
        if (session.getAttribute("userId") == null) {
            return "redirect:/login";
        }

        model.addAttribute("medicament", new Medicament());
        return "utilisateur/ajouter-medicament"; // formulaire HTML
    }

    // enregistrer un médicament
    @PostMapping("/ajouter/medicament")
    public String enregistrerMedicament(@ModelAttribute Medicament medicament, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        Utilisateur utilisateur = utilisateurRepo.findById(userId).orElse(null);
        medicament.setUtilisateur(utilisateur);
        medicamentRepo.save(medicament);
        return "redirect:/medicament";
    }

    // afficher formulaire de modification
    @GetMapping("/modifier/medicament/{id}")
    public String formModifier(@PathVariable Long id, Model model, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        Medicament medicament = medicamentRepo.findById(id).orElse(null);
        if (medicament == null || !medicament.getUtilisateur().getId().equals(userId)) {
            return "redirect:/medicament";
        }

        model.addAttribute("medicament", medicament);
        return "utilisateur/modifier-medicament";
    }

    // enregistrer modification
    @PostMapping("/modifier/medicament")
    public String enregistrerModification(@ModelAttribute Medicament medicament, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        Utilisateur utilisateur = utilisateurRepo.findById(userId).orElse(null);
        medicament.setUtilisateur(utilisateur);
        medicamentRepo.save(medicament);
        return "redirect:/medicament";
    }

    // supprimer médicament
    @GetMapping("/supprimer/medicament/{id}")
    public String supprimer(@PathVariable Long id, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        Medicament medicament = medicamentRepo.findById(id).orElse(null);
        if (medicament != null && medicament.getUtilisateur().getId().equals(userId)) {
            medicamentRepo.deleteById(id);
        }

        return "redirect:/medicament";
    }
}
