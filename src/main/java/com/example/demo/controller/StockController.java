package com.example.demo.controller;

import com.example.demo.model.Medpharmacie;
import com.example.demo.model.Notification;
import com.example.demo.repository.MedpharmacieRepository;
import com.example.demo.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/pharmacie/{pharmacieId}/gestion-stock")
public class StockController {

    @Autowired
    private MedpharmacieRepository medpharmacieRepository;

    @Autowired
    private NotificationService notificationService;

    // 📌 Afficher la gestion du stock
    @GetMapping
    public String gestionStock(@PathVariable Long pharmacieId, Model model) {
        try {
            List<Medpharmacie> medicaments = medpharmacieRepository.findByPharmacieId(pharmacieId);
            model.addAttribute("medicaments", medicaments);
            model.addAttribute("pharmacieId", pharmacieId);

            List<Medpharmacie> stocksEpuises = medicaments.stream()
                    .filter(med -> med.getQuantite() != null && med.getQuantite() == 0)
                    .toList();

            System.out.println("📊 Stocks épuisés trouvés: " + stocksEpuises.size());
            for (Medpharmacie med : stocksEpuises) {
                System.out.println("➡️ Vérification: " + med.getNom() + " (qte: " + med.getQuantite() + ")");
                boolean result = notificationService.createStockAlert(med, pharmacieId);
                System.out.println("📝 Résultat alerte: " + result);
            }

            List<Notification> notifications = notificationService.getUnreadNotifications(pharmacieId);
            model.addAttribute("notifications", notifications);
            model.addAttribute("unreadCount", notifications != null ? notifications.size() : 0);

            return "pharmacie/gestion-stock";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Erreur lors du chargement du stock: " + e.getMessage());
            return "error";
        }
    }

    // 📌 Formulaire ajout
    @GetMapping("/ajouter")
    public String showAjouterForm(@PathVariable Long pharmacieId, Model model) {
        model.addAttribute("medicament", new Medpharmacie());
        model.addAttribute("pharmacieId", pharmacieId);
        return "pharmacie/ajouter";
    }

    // 📌 Ajouter médicament
    @PostMapping("/ajouter")
    public String ajouterMedicament(
            @PathVariable Long pharmacieId,
            @RequestParam String nom,
            @RequestParam String description,
            @RequestParam Double prix,
            @RequestParam Integer quantite,
            @RequestParam(required = false) Boolean ordonnanceRequise,
            @RequestParam(required = false) MultipartFile photo,
            Model model) {
        try {
            if (ordonnanceRequise == null) ordonnanceRequise = false;

            Medpharmacie medicament = new Medpharmacie();
            medicament.setNom(nom);
            medicament.setDescription(description);
            medicament.setPrix(prix);
            medicament.setQuantite(quantite);
            medicament.setOrdonnanceRequise(ordonnanceRequise);
            medicament.setPharmacieId(pharmacieId);

            if (photo != null && !photo.isEmpty()) {
                String photoData = Base64.getEncoder().encodeToString(photo.getBytes());
                medicament.setPhotoData(photoData);
            }

            medpharmacieRepository.save(medicament);
            System.out.println("💊 Médicament ajouté: " + nom + " (qte: " + quantite + ")");

            if (quantite == 0) {
                System.out.println("⚠️ Tentative création alerte pour nouveau médicament en rupture");
                boolean result = notificationService.createStockAlert(medicament, pharmacieId);
                System.out.println("📝 Résultat alerte: " + result);
            }

            return "redirect:/pharmacie/" + pharmacieId + "/gestion-stock?success=Médicament ajouté avec succès";
        } catch (IOException e) {
            e.printStackTrace();
            model.addAttribute("error", "Erreur lors du traitement de l'image: " + e.getMessage());
            return "error";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Erreur lors de l'ajout du médicament: " + e.getMessage());
            return "error";
        }
    }

    // 📌 Formulaire modification
    @GetMapping("/modifier/{medicamentId}")
    public String showModifierForm(@PathVariable Long pharmacieId, @PathVariable Long medicamentId, Model model) {
        try {
            Optional<Medpharmacie> medicamentOpt = medpharmacieRepository.findByIdAndPharmacieId(medicamentId, pharmacieId);
            if (medicamentOpt.isEmpty()) {
                model.addAttribute("error", "Médicament non trouvé");
                return "error";
            }
            model.addAttribute("medicament", medicamentOpt.get());
            model.addAttribute("pharmacieId", pharmacieId);
            return "pharmacie/modifier";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Erreur lors du chargement du médicament: " + e.getMessage());
            return "error";
        }
    }

    // 📌 Modifier médicament
    @PostMapping("/modifier/{medicamentId}")
    public String modifierMedicament(
            @PathVariable Long pharmacieId,
            @PathVariable Long medicamentId,
            @RequestParam String nom,
            @RequestParam String description,
            @RequestParam Double prix,
            @RequestParam Integer quantite,
            @RequestParam(required = false) Boolean ordonnanceRequise,
            @RequestParam(required = false) MultipartFile photo,
            Model model) {
        try {
            if (ordonnanceRequise == null) ordonnanceRequise = false;

            Optional<Medpharmacie> medicamentOpt = medpharmacieRepository.findByIdAndPharmacieId(medicamentId, pharmacieId);
            if (medicamentOpt.isEmpty()) {
                model.addAttribute("error", "Médicament non trouvé");
                return "error";
            }

            Medpharmacie medicament = medicamentOpt.get();
            Integer ancienneQuantite = medicament.getQuantite();

            medicament.setNom(nom);
            medicament.setDescription(description);
            medicament.setPrix(prix);
            medicament.setQuantite(quantite);
            medicament.setOrdonnanceRequise(ordonnanceRequise);

            if (photo != null && !photo.isEmpty()) {
                String photoData = Base64.getEncoder().encodeToString(photo.getBytes());
                medicament.setPhotoData(photoData);
            }

            medpharmacieRepository.save(medicament);
            System.out.println("✏️ Médicament modifié: " + nom + " (ancienne qte: " + ancienneQuantite + ", nouvelle qte: " + quantite + ")");

            if (ancienneQuantite != null && ancienneQuantite > 0 && quantite == 0) {
                System.out.println("⚠️ Tentative création alerte pour médicament maintenant en rupture");
                boolean result = notificationService.createStockAlert(medicament, pharmacieId);
                System.out.println("📝 Résultat alerte: " + result);
            }

            return "redirect:/pharmacie/" + pharmacieId + "/gestion-stock?success=Médicament modifié avec succès";
        } catch (IOException e) {
            e.printStackTrace();
            model.addAttribute("error", "Erreur lors du traitement de l'image: " + e.getMessage());
            return "error";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Erreur lors de la modification du médicament: " + e.getMessage());
            return "error";
        }
    }

    // 📌 Supprimer médicament
    @PostMapping("/supprimer/{medicamentId}")
    public String supprimerMedicament(@PathVariable Long pharmacieId, @PathVariable Long medicamentId, Model model) {
        try {
            if (!medpharmacieRepository.existsByIdAndPharmacieId(medicamentId, pharmacieId)) {
                model.addAttribute("error", "Médicament non trouvé");
                return "error";
            }
            medpharmacieRepository.deleteById(medicamentId);
            return "redirect:/pharmacie/" + pharmacieId + "/gestion-stock?success=Médicament supprimé avec succès";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Erreur lors de la suppression du médicament: " + e.getMessage());
            return "error";
        }
    }

    // 📌 Vérifier les stocks
    @GetMapping("/verifier-stocks")
    public String verifierStocks(@PathVariable Long pharmacieId, Model model) {
        try {
            List<Medpharmacie> medicaments = medpharmacieRepository.findByPharmacieId(pharmacieId);
            List<Medpharmacie> stocksEpuises = medicaments.stream()
                    .filter(med -> med.getQuantite() != null && med.getQuantite() == 0)
                    .toList();

            System.out.println("🔍 Vérification stocks - Épuisés: " + stocksEpuises.size());
            int alertesCrees = 0;
            for (Medpharmacie med : stocksEpuises) {
                System.out.println("➡️ Vérification stock: " + med.getNom() + " (qte: " + med.getQuantite() + ")");
                boolean alerteCreee = notificationService.createStockAlert(med, pharmacieId);
                if (alerteCreee) {
                    alertesCrees++;
                    System.out.println("✅ Alerte créée: " + med.getNom());
                } else {
                    System.out.println("❌ Alerte non créée: " + med.getNom());
                }
            }

            return "redirect:/pharmacie/" + pharmacieId + "/gestion-stock?info=" + alertesCrees + " alerte(s) créée(s)";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Erreur lors de la vérification des stocks: " + e.getMessage());
            return "error";
        }
    }

    // 📌 Recherche
    @GetMapping("/rechercher")
    public String rechercherMedicaments(@PathVariable Long pharmacieId, @RequestParam String nom, Model model) {
        try {
            List<Medpharmacie> medicaments = medpharmacieRepository.findByPharmacieIdAndNomContainingIgnoreCase(pharmacieId, nom);
            model.addAttribute("medicaments", medicaments);
            model.addAttribute("termerecherche", nom);
            model.addAttribute("pharmacieId", pharmacieId);
            return "pharmacie/gestion-stock";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Erreur lors de la recherche: " + e.getMessage());
            return "error";
        }
    }

    // 📌 API count notifs
    @GetMapping("/api/notifications/count")
    @ResponseBody
    public int getNotificationCount(@PathVariable Long pharmacieId) {
        try {
            return notificationService.getUnreadCount(pharmacieId);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }}
