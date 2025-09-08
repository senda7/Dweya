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

    // Page principale de gestion du stock
    @GetMapping
    public String gestionStock(@PathVariable Long pharmacieId, Model model) {
        try {
            // Récupérer tous les médicaments de la pharmacie spécifique
            List<Medpharmacie> medicaments = medpharmacieRepository.findByPharmacieId(pharmacieId);
            model.addAttribute("medicaments", medicaments);
            model.addAttribute("pharmacieId", pharmacieId);

            // Vérifier les stocks épuisés
            List<Medpharmacie> stocksEpuises = medicaments.stream()
                    .filter(med -> med.getQuantite() != null && med.getQuantite() == 0)
                    .toList();

            // Créer des notifications
            for (Medpharmacie med : stocksEpuises) {
                notificationService.createStockAlert(med, pharmacieId);
            }

            // Récupérer les notifications
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

    // Afficher le formulaire d'ajout - CORRIGÉ
    @GetMapping("/ajouter")
    public String showAjouterForm(@PathVariable Long pharmacieId, Model model) {
        model.addAttribute("medicament", new Medpharmacie());
        model.addAttribute("pharmacieId", pharmacieId);
        return "pharmacie/ajouter"; // ← CORRECTION ICI
    }

    // Ajouter un nouveau médicament
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
            if (ordonnanceRequise == null) {
                ordonnanceRequise = false;
            }

            Medpharmacie medicament = new Medpharmacie();
            medicament.setNom(nom);
            medicament.setDescription(description);
            medicament.setPrix(prix);
            medicament.setQuantite(quantite);
            medicament.setOrdonnanceRequise(ordonnanceRequise);
            medicament.setPharmacieId(pharmacieId);

            // Gestion de la photo
            if (photo != null && !photo.isEmpty()) {
                String photoData = Base64.getEncoder().encodeToString(photo.getBytes());
                medicament.setPhotoData(photoData);
            }

            medpharmacieRepository.save(medicament);

            // Vérifier si le stock est épuisé
            if (quantite == 0) {
                notificationService.createStockAlert(medicament, pharmacieId);
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

    // Afficher le formulaire de modification - CORRIGÉ
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
            return "pharmacie/modifier"; // ← CORRECTION ICI

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Erreur lors du chargement du médicament: " + e.getMessage());
            return "error";
        }
    }

    // Modifier un médicament existant
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
            if (ordonnanceRequise == null) {
                ordonnanceRequise = false;
            }

            Optional<Medpharmacie> medicamentOpt = medpharmacieRepository.findByIdAndPharmacieId(medicamentId, pharmacieId);
            if (medicamentOpt.isEmpty()) {
                model.addAttribute("error", "Médicament non trouvé");
                return "error";
            }

            Medpharmacie medicament = medicamentOpt.get();

            // Sauvegarder l'ancienne quantité
            Integer ancienneQuantite = medicament.getQuantite();

            medicament.setNom(nom);
            medicament.setDescription(description);
            medicament.setPrix(prix);
            medicament.setQuantite(quantite);
            medicament.setOrdonnanceRequise(ordonnanceRequise);

            // Gestion de la photo
            if (photo != null && !photo.isEmpty()) {
                String photoData = Base64.getEncoder().encodeToString(photo.getBytes());
                medicament.setPhotoData(photoData);
            }

            medpharmacieRepository.save(medicament);

            // Vérifier les alertes de stock
            if (ancienneQuantite != null && ancienneQuantite > 0 && quantite == 0) {
                notificationService.createStockAlert(medicament, pharmacieId);
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

    // Supprimer un médicament
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

    // Vérifier les stocks épuisés
    @GetMapping("/verifier-stocks")
    public String verifierStocks(@PathVariable Long pharmacieId, Model model) {
        try {
            List<Medpharmacie> medicaments = medpharmacieRepository.findByPharmacieId(pharmacieId);
            List<Medpharmacie> stocksEpuises = medicaments.stream()
                    .filter(med -> med.getQuantite() != null && med.getQuantite() == 0)
                    .toList();

            int alertesCrees = 0;
            for (Medpharmacie med : stocksEpuises) {
                boolean alerteCreee = notificationService.createStockAlert(med, pharmacieId);
                if (alerteCreee) {
                    alertesCrees++;
                }
            }

            return "redirect:/pharmacie/" + pharmacieId + "/gestion-stock?info=" + alertesCrees + " alerte(s) de stock créée(s)";

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Erreur lors de la vérification des stocks: " + e.getMessage());
            return "error";
        }
    }

    // Rechercher des médicaments
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

    // API pour obtenir le nombre de notifications non lues
    @GetMapping("/api/notifications/count")
    @ResponseBody
    public int getNotificationCount(@PathVariable Long pharmacieId) {
        try {
            return notificationService.getUnreadCount(pharmacieId);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    // TEST METHOD - Pour vérifier que le contrôleur fonctionne (CORRIGÉ)
    @GetMapping("/test")
    @ResponseBody
    public String test(@PathVariable Long pharmacieId) {
        return "✅ StockController est fonctionnel pour la pharmacie ID: " + pharmacieId;
    }

    // Page d'accueil pour choisir une pharmacie (optionnel)
    @GetMapping("/accueil")
    public String accueil(Model model) {
        // Vous pouvez ajouter une liste des pharmacies disponibles si nécessaire
        return "pharmacie/accueil";
    }
}