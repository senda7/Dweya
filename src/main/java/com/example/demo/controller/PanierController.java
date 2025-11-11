package com.example.demo.controller;

import com.example.demo.model.Medpharmacie;
import com.example.demo.model.MedicamentCommande;
import com.example.demo.model.Commande;
import com.example.demo.model.Utilisateur;
import com.example.demo.service.MedpharmacieService;
import com.example.demo.service.CommandeService;
import com.example.demo.service.PharmacieService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
public class PanierController {

    @Autowired
    private PharmacieService pharmacieService;

    @Autowired
    private MedpharmacieService medpharmacieService;

    @Autowired
    private CommandeService commandeService;

    // Classe interne pour représenter un item du panier
    public static class CommandeItem {
        private Medpharmacie medicament;
        private int quantite;

        public CommandeItem(Medpharmacie medicament, int quantite) {
            this.medicament = medicament;
            this.quantite = quantite;
        }

        public Medpharmacie getMedicament() {
            return medicament;
        }

        public int getQuantite() {
            return quantite;
        }
    }

    // Affichage du stock de la pharmacie
    @GetMapping("/pharmacie/{pharmacieId}/stock")
    public String getStockPharmacie(@PathVariable Long pharmacieId, HttpSession session, Model model,
                                    @RequestParam(value = "orderSuccess", required = false) Boolean orderSuccess) {
        Utilisateur utilisateur = (Utilisateur) session.getAttribute("utilisateurConnecte");
        if (utilisateur == null) {
            return "redirect:/login";
        }

        Optional<Utilisateur> pharmacieOpt = pharmacieService.findById(pharmacieId);
        if (pharmacieOpt.isEmpty()) {
            model.addAttribute("error", "Pharmacie non trouvée");
            return "error";
        }

        Utilisateur pharmacie = pharmacieOpt.get();
        List<Medpharmacie> medicaments = medpharmacieService.getMedicamentsByPharmacie(pharmacieId);

        model.addAttribute("pharmacie", pharmacie);
        model.addAttribute("medicaments", medicaments);

        if (orderSuccess != null && orderSuccess) {
            model.addAttribute("success", "Votre commande a été passée avec succès! Votre panier a été vidé.");
        }

        return "pharmacie-stock";
    }

    // Étape 1 : Affichage du panier avant validation
    @GetMapping("/commander-panier")
    public String showCommanderPanier(@RequestParam Long pharmacieId,
                                      @RequestParam List<Long> medicamentIds,
                                      @RequestParam List<Integer> quantites,
                                      HttpSession session,
                                      Model model) {
        Utilisateur utilisateur = (Utilisateur) session.getAttribute("utilisateurConnecte");
        if (utilisateur == null) {
            return "redirect:/login";
        }

        List<CommandeItem> items = new ArrayList<>();
        double total = 0;

        for (int i = 0; i < medicamentIds.size(); i++) {
            Optional<Medpharmacie> medOpt = medpharmacieService.getMedicamentById(medicamentIds.get(i));
            if (medOpt.isPresent()) {
                Medpharmacie med = medOpt.get();
                int qte = quantites.get(i);

                if (qte <= 0) {
                    model.addAttribute("error", "Quantité invalide pour le médicament: " + med.getNom());
                    return "error";
                }

                items.add(new CommandeItem(med, qte));
                total += med.getPrix() * qte;
            } else {
                model.addAttribute("error", "Médicament non trouvé");
                return "error";
            }
        }

        Optional<Utilisateur> pharmaOpt = pharmacieService.findById(pharmacieId);
        if (pharmaOpt.isEmpty()) {
            model.addAttribute("error", "Pharmacie non trouvée");
            return "error";
        }

        Utilisateur pharma = pharmaOpt.get();

        session.setAttribute("commande_medicamentIds", medicamentIds);
        session.setAttribute("commande_quantites", quantites);
        session.setAttribute("commande_pharmacieId", pharmacieId);

        Commande commande = new Commande();
        commande.setNom(utilisateur.getNom());
        commande.setPrenom(utilisateur.getPrenom());
        commande.setEmail(utilisateur.getEmail());
        commande.setTelephone(utilisateur.getTelephone());
        commande.setAdresse(utilisateur.getAdresse());

        model.addAttribute("commande", commande);
        model.addAttribute("items", items);
        model.addAttribute("total", total);
        model.addAttribute("pharmacie", pharma);

        return "commander-panier";
    }

    // Étape 2 : Validation et enregistrement de la commande
    @PostMapping("/commander-panier")
    public String processCommanderPanier(@ModelAttribute Commande commande,
                                         HttpSession session,
                                         Model model,
                                         RedirectAttributes redirectAttributes) {
        Utilisateur utilisateur = (Utilisateur) session.getAttribute("utilisateurConnecte");
        if (utilisateur == null) {
            return "redirect:/login";
        }

        List<Long> medicamentIds = (List<Long>) session.getAttribute("commande_medicamentIds");
        List<Integer> quantites = (List<Integer>) session.getAttribute("commande_quantites");
        Long pharmacieId = (Long) session.getAttribute("commande_pharmacieId");

        if (medicamentIds == null || quantites == null || pharmacieId == null) {
            model.addAttribute("error", "Données de commande manquantes. Veuillez recommencer.");
            return "commander-panier";
        }

        try {
            Commande nouvelleCommande = new Commande();
            nouvelleCommande.setUtilisateurId(utilisateur.getId());
            nouvelleCommande.setPharmacieId(pharmacieId);
            nouvelleCommande.setNom(commande.getNom());
            nouvelleCommande.setPrenom(commande.getPrenom());
            nouvelleCommande.setAdresse(commande.getAdresse());
            nouvelleCommande.setTelephone(commande.getTelephone());
            nouvelleCommande.setEmail(commande.getEmail());
            nouvelleCommande.setNotes(commande.getNotes());
            nouvelleCommande.setDateCommande(LocalDateTime.now());
            nouvelleCommande.setStatut("EN_ATTENTE");

            double total = 0;

            for (int i = 0; i < medicamentIds.size(); i++) {
                Long medicamentId = medicamentIds.get(i);
                int quantite = quantites.get(i);

                Medpharmacie med = medpharmacieService.getMedicamentById(medicamentId)
                        .orElseThrow(() -> new RuntimeException("Médicament non trouvé"));

                if (med.getQuantite() < quantite) {
                    model.addAttribute("error", "Stock insuffisant pour le médicament: " + med.getNom());
                    return reloadCommanderPanier(model, medicamentIds, quantites, pharmacieId, commande);
                }

                // ✅ Création du lien médicament-commande
                MedicamentCommande medCmd = new MedicamentCommande();
                medCmd.setNom(med.getNom());
                medCmd.setPrix(med.getPrix());
                medCmd.setQuantite(quantite);
                medCmd.setCommande(nouvelleCommande);

                nouvelleCommande.getMedicaments().add(medCmd);

                // ✅ Mise à jour du stock
                med.setQuantite(med.getQuantite() - quantite);
                medpharmacieService.save(med);

                total += med.getPrix() * quantite;
            }

            nouvelleCommande.setPrixTotal(total);
            commandeService.save(nouvelleCommande);

            // Nettoyage de la session
            session.removeAttribute("commande_medicamentIds");
            session.removeAttribute("commande_quantites");
            session.removeAttribute("commande_pharmacieId");

            session.setAttribute("lastOrderId", nouvelleCommande.getId());
            return "redirect:/confirmation/" + nouvelleCommande.getId();

        } catch (Exception e) {
            model.addAttribute("error", "Erreur lors de la commande: " + e.getMessage());
            return reloadCommanderPanier(model, medicamentIds, quantites, pharmacieId, commande);
        }
    }

    // Méthode utilitaire pour recharger la page en cas d'erreur
    private String reloadCommanderPanier(Model model, List<Long> medicamentIds, List<Integer> quantites, Long pharmacieId, Commande commande) {
        List<CommandeItem> items = new ArrayList<>();
        double total = 0;

        for (int i = 0; i < medicamentIds.size(); i++) {
            Optional<Medpharmacie> medOpt = medpharmacieService.getMedicamentById(medicamentIds.get(i));
            if (medOpt.isPresent()) {
                Medpharmacie med = medOpt.get();
                int qte = quantites.get(i);

                items.add(new CommandeItem(med, qte));
                total += med.getPrix() * qte;
            }
        }

        pharmacieService.findById(pharmacieId).ifPresent(pharma -> model.addAttribute("pharmacie", pharma));

        model.addAttribute("items", items);
        model.addAttribute("total", total);
        model.addAttribute("commande", commande);

        return "commander-panier";
    }
}