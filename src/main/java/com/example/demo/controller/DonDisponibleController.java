package com.example.demo.controller;

import com.example.demo.model.Don;
import com.example.demo.model.StatutDon;
import com.example.demo.model.Utilisateur;
import com.example.demo.repository.DonRepository;
import com.example.demo.repository.UtilisateurRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Controller
public class DonDisponibleController {

    private final DonRepository donRepository;
    private final UtilisateurRepository utilisateurRepository;

    @Autowired
    private JavaMailSender mailSender;

    public DonDisponibleController(DonRepository donRepository,
                                   UtilisateurRepository utilisateurRepository) {
        this.donRepository = donRepository;
        this.utilisateurRepository = utilisateurRepository;
    }

    // =================== Liste des dons disponibles ===================
    @GetMapping("/dons-disponibles")
    public String afficherDonsDisponibles(@RequestParam(value = "ville", required = false) String ville,
                                          @RequestParam(value = "nomPharmacie", required = false) String nomPharmacie,
                                          @RequestParam(value = "nomMedicament", required = false) String nomMedicament,
                                          Model model) {

        // récupérer uniquement dons acceptés ET sans codeDemande
        List<Don> dons = donRepository.findByStatut(StatutDon.ACCEPTE);
        dons.removeIf(d -> d.getCodeDemande() != null && !d.getCodeDemande().isEmpty());

        // filtrage
        if (ville != null && !ville.isEmpty()) {
            dons.removeIf(d -> d.getPharmacie() == null ||
                    d.getPharmacie().getVille() == null ||
                    !d.getPharmacie().getVille().toLowerCase().contains(ville.toLowerCase()));
        }
        if (nomMedicament != null && !nomMedicament.isEmpty()) {
            dons.removeIf(d -> d.getMedicament() == null ||
                    d.getMedicament().getNom() == null ||
                    !d.getMedicament().getNom().toLowerCase().contains(nomMedicament.toLowerCase()));
        }

        model.addAttribute("donsDisponibles", dons);
        model.addAttribute("ville", ville);
        model.addAttribute("nomPharmacie", nomPharmacie);
        model.addAttribute("nomMedicament", nomMedicament);

        return "utilisateur/dons-disponibles";
    }

    // =================== Je le veux ===================
    @GetMapping("/dons/je-le-veux/{id}")
    public String jeLeVeux(@PathVariable("id") Long id,
                           HttpSession session,
                           Model model) {

        Long utilisateurId = (Long) session.getAttribute("userId"); // utilisateur connecté
        if (utilisateurId == null) {
            model.addAttribute("error", " Veuillez vous connecter pour demander un don.");
            return "redirect:/login";
        }

        Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId).orElse(null);
        if (utilisateur == null) {
            model.addAttribute("error", "Utilisateur introuvable.");
            return "redirect:/login";
        }

        Don don = donRepository.findById(id).orElse(null);
        if (don == null || don.getStatut() != StatutDon.ACCEPTE || don.getCodeDemande() != null) {
            model.addAttribute("error", " Ce don n'est plus disponible.");
        } else {
            // générer code unique
            String code = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            don.setCodeDemande(code);
            donRepository.save(don);

            // envoyer email
            try {
                String contenu = "Bonjour " + utilisateur.getNom() + " " + utilisateur.getPrenom() + ",\n\n"
                        + "Votre demande pour le don a bien été enregistrée.\n\n"
                        + "👉 Code Demande : " + code + "\n"
                        + "👉 Médicament : " + (don.getMedicament() != null ? don.getMedicament().getNom() : "Non défini") + "\n"
                        + "👉 ID Don : " + don.getId() + "\n"
                        + "👉 Pharmacie : " + (don.getPharmacie() != null ? don.getPharmacie().getNomPharmacie() : "Non définie") + "\n\n"
                        + "⚠️ Merci de récupérer ce don dans un délai maximum de 2 jours.\n\n"
                        + "Merci d'utiliser Dwaya.";

                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(utilisateur.getEmail());
                message.setSubject("Confirmation de votre demande de don");
                message.setText(contenu);

                mailSender.send(message);

                model.addAttribute("message", "Votre demande a été traitée. Un email contenant les informations nécessaires vous a été envoyé.");
            } catch (Exception e) {
                model.addAttribute("error", "Erreur lors de l'envoi de l'email : " + e.getMessage());
            }
        }

        // recharger liste dons dispo (sans codeDemande)
        List<Don> dons = donRepository.findByStatut(StatutDon.ACCEPTE);
        dons.removeIf(d -> d.getCodeDemande() != null && !d.getCodeDemande().isEmpty());

        model.addAttribute("donsDisponibles", dons);

        return "utilisateur/dons-disponibles";
    }
}
