package com.example.demo.controller;

import com.example.demo.model.Medpharmacie;
import com.example.demo.model.Notification;
import com.example.demo.model.Utilisateur;
import com.example.demo.repository.MedpharmacieRepository;
import com.example.demo.repository.UtilisateurRepository;
import com.example.demo.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/pharmacie/{pharmacieId}/gestion-stock")
public class StockController {

    @Autowired
    private MedpharmacieRepository medpharmacieRepository;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private NotificationService notificationService;

    // Méthode utilitaire pour convertir Date en LocalDate
    private LocalDate convertToLocalDate(Date date) {
        if (date == null) {
            return null;
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    // Page principale de gestion du stock
    @GetMapping
    public String gestionStock(@PathVariable Long pharmacieId, Model model) {
        try {
            System.out.println("=== CHARGEMENT STOCK PHARMACIE " + pharmacieId + " ===");

            // Récupérer l'utilisateur
            Utilisateur utilisateur = utilisateurRepository.findById(pharmacieId).orElse(null);
            if (utilisateur != null) {
                model.addAttribute("utilisateur", utilisateur);
                System.out.println("✅ Utilisateur trouvé: " + utilisateur.getNomPharmacie());
            } else {
                System.out.println("⚠️ Utilisateur non trouvé pour ID: " + pharmacieId);
                model.addAttribute("utilisateur", null);
            }

            // Récupérer tous les médicaments de la pharmacie
            List<Medpharmacie> medicaments = medpharmacieRepository.findByPharmacieId(pharmacieId);
            model.addAttribute("medicaments", medicaments);
            model.addAttribute("pharmacieId", pharmacieId);

            System.out.println("📦 Médicaments trouvés: " + medicaments.size());

            // Vérifier les ruptures de stock et créer des notifications
            notificationService.checkAllMedicamentsForStockAlerts(medicaments, pharmacieId);

            // Récupérer les notifications non lues
            List<Notification> notifications = notificationService.getUnreadNotifications(pharmacieId);
            model.addAttribute("notifications", notifications);
            model.addAttribute("unreadCount", notifications != null ? notifications.size() : 0);

            System.out.println("🔔 Notifications non lues: " + (notifications != null ? notifications.size() : 0));
            System.out.println("✅ Page de gestion de stock chargée avec succès");

            return "pharmacie/gestion-stock";

        } catch (Exception e) {
            System.out.println("❌ ERREUR lors du chargement du stock: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Erreur lors du chargement du stock: " + e.getMessage());
            return "error";
        }
    }

    // Afficher le formulaire d'ajout
    @GetMapping("/ajouter")
    public String showAjouterForm(@PathVariable Long pharmacieId, Model model) {
        try {
            // Récupérer l'utilisateur pour l'affichage
            Utilisateur utilisateur = utilisateurRepository.findById(pharmacieId).orElse(null);
            if (utilisateur != null) {
                model.addAttribute("utilisateur", utilisateur);
            }

            model.addAttribute("medicament", new Medpharmacie());
            model.addAttribute("pharmacieId", pharmacieId);

            System.out.println("📋 Affichage formulaire d'ajout pour pharmacie: " + pharmacieId);
            return "pharmacie/ajouter";

        } catch (Exception e) {
            System.out.println("❌ Erreur formulaire ajout: " + e.getMessage());
            model.addAttribute("error", "Erreur lors du chargement du formulaire");
            return "error";
        }
    }

    // Ajouter un nouveau médicament
    @PostMapping("/ajouter")
    public String ajouterMedicament(
            @PathVariable Long pharmacieId,
            @RequestParam String nom,
            @RequestParam String description,
            @RequestParam Double prix,
            @RequestParam Integer quantite,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date datePeremption,
            @RequestParam(required = false) Boolean ordonnanceRequise,
            @RequestParam(required = false) MultipartFile photo,RedirectAttributes redirectAttributes,
            Model model) {

        try {
            System.out.println("=== AJOUT MÉDICAMENT: " + nom + " ===");
            System.out.println("📊 Quantité: " + quantite + ", Prix: " + prix);

            if (ordonnanceRequise == null) {
                ordonnanceRequise = false;
            }

            Medpharmacie medicament = new Medpharmacie();
            medicament.setNom(nom);
            medicament.setDescription(description);
            medicament.setPrix(prix);
            medicament.setQuantite(quantite);
            medicament.setDatePeremption(convertToLocalDate(datePeremption));
            medicament.setOrdonnanceRequise(ordonnanceRequise);
            medicament.setPharmacieId(pharmacieId);

            // Gestion de la photo
            if (photo != null && !photo.isEmpty()) {
                String photoData = Base64.getEncoder().encodeToString(photo.getBytes());
                medicament.setPhotoData(photoData);
                System.out.println("📸 Photo ajoutée");
            }

            medpharmacieRepository.save(medicament);
            System.out.println("💾 Médicament sauvegardé en base");

            // Vérifier si le stock est épuisé
            if (quantite == 0) {
                System.out.println("⚠️ Médicament ajouté avec quantité 0, création alerte...");
                notificationService.createStockAlert(medicament, pharmacieId);
            } else {
                System.out.println("✅ Médicament ajouté avec succès");
            }

            redirectAttributes.addFlashAttribute("successMessage", "Médicament ajouté avec succès");
            return "redirect:/pharmacie/" + pharmacieId + "/gestion-stock";

        } catch (IOException e) {
            System.out.println("❌ Erreur traitement image: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Erreur lors du traitement de l'image: " + e.getMessage());
            return "error";
        } catch (Exception e) {
            System.out.println("❌ Erreur ajout médicament: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Erreur lors de l'ajout du médicament: " + e.getMessage());
            return "error";
        }
    }

    // Afficher le formulaire de modification
    @GetMapping("/modifier/{medicamentId}")
    public String showModifierForm(@PathVariable Long pharmacieId, @PathVariable Long medicamentId, Model model) {
        try {
            // Récupérer l'utilisateur
            Utilisateur utilisateur = utilisateurRepository.findById(pharmacieId).orElse(null);
            if (utilisateur != null) {
                model.addAttribute("utilisateur", utilisateur);
            }

            Optional<Medpharmacie> medicamentOpt = medpharmacieRepository.findByIdAndPharmacieId(medicamentId, pharmacieId);
            if (medicamentOpt.isEmpty()) {
                System.out.println("❌ Médicament non trouvé: " + medicamentId);
                model.addAttribute("error", "Médicament non trouvé");
                return "error";
            }

            model.addAttribute("medicament", medicamentOpt.get());
            model.addAttribute("pharmacieId", pharmacieId);

            System.out.println("📋 Affichage formulaire modification médicament: " + medicamentId);
            return "pharmacie/modifier";

        } catch (Exception e) {
            System.out.println("❌ Erreur formulaire modification: " + e.getMessage());
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
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date datePeremption,
            @RequestParam(required = false) Boolean ordonnanceRequise,
            @RequestParam(required = false) MultipartFile photo,RedirectAttributes redirectAttributes,
            Model model) {

        try {
            System.out.println("=== MODIFICATION MÉDICAMENT ID: " + medicamentId + " ===");

            if (ordonnanceRequise == null) {
                ordonnanceRequise = false;
            }

            Optional<Medpharmacie> medicamentOpt = medpharmacieRepository.findByIdAndPharmacieId(medicamentId, pharmacieId);
            if (medicamentOpt.isEmpty()) {
                System.out.println("❌ Médicament non trouvé: " + medicamentId);
                model.addAttribute("error", "Médicament non trouvé");
                return "error";
            }

            Medpharmacie medicament = medicamentOpt.get();

            // Sauvegarder l'ancienne quantité
            Integer ancienneQuantite = medicament.getQuantite();
            System.out.println("📊 Ancienne quantité: " + ancienneQuantite + ", Nouvelle quantité: " + quantite);

            medicament.setNom(nom);
            medicament.setDescription(description);
            medicament.setPrix(prix);
            medicament.setQuantite(quantite);
            medicament.setDatePeremption(convertToLocalDate(datePeremption));
            medicament.setOrdonnanceRequise(ordonnanceRequise);

            // Gestion de la photo
            if (photo != null && !photo.isEmpty()) {
                String photoData = Base64.getEncoder().encodeToString(photo.getBytes());
                medicament.setPhotoData(photoData);
                System.out.println("📸 Photo modifiée");
            }

            medpharmacieRepository.save(medicament);
            System.out.println("💾 Médicament modifié avec succès");

            // Vérifier les alertes de stock - seulement si passage de >0 à 0
            if (ancienneQuantite != null && ancienneQuantite > 0 && quantite == 0) {
                System.out.println("⚠️ Rupture de stock détectée, création alerte...");
                notificationService.createStockAlert(medicament, pharmacieId);
            }

            redirectAttributes.addFlashAttribute("successMessage", "Médicament modifié avec succès");
            return "redirect:/pharmacie/" + pharmacieId + "/gestion-stock";

        } catch (IOException e) {
            System.out.println("❌ Erreur traitement image: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Erreur lors du traitement de l'image: " + e.getMessage());
            return "error";
        } catch (Exception e) {
            System.out.println("❌ Erreur modification médicament: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Erreur lors de la modification du médicament: " + e.getMessage());
            return "error";
        }
    }

    // Supprimer un médicament
    @PostMapping("/supprimer/{medicamentId}")
    public String supprimerMedicament(@PathVariable Long pharmacieId, @PathVariable Long medicamentId,RedirectAttributes redirectAttributes, Model model) {
        try {
            System.out.println("=== SUPPRESSION MÉDICAMENT ID: " + medicamentId + " ===");

            if (!medpharmacieRepository.existsByIdAndPharmacieId(medicamentId, pharmacieId)) {
                System.out.println("❌ Médicament non trouvé: " + medicamentId);
                model.addAttribute("error", "Médicament non trouvé");
                return "error";
            }

            medpharmacieRepository.deleteById(medicamentId);
            System.out.println("✅ Médicament supprimé avec succès");

            redirectAttributes.addFlashAttribute("successMessage", "Médicament supprimé avec succès");
            return "redirect:/pharmacie/" + pharmacieId + "/gestion-stock";

        } catch (Exception e) {
            System.out.println("❌ Erreur suppression médicament: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Erreur lors de la suppression du médicament: " + e.getMessage());
            return "error";
        }
    }

    // Vérifier les stocks épuisés manuellement
    @GetMapping("/verifier-stocks")
    public String verifierStocks(@PathVariable Long pharmacieId,  RedirectAttributes redirectAttributes,Model model) {
        try {
            System.out.println("=== VÉRIFICATION MANUELLE DES STOCKS ===");

            List<Medpharmacie> medicaments = medpharmacieRepository.findByPharmacieId(pharmacieId);
            System.out.println("🔍 Vérification de " + medicaments.size() + " médicaments");

            // Utiliser la méthode de vérification groupée
            notificationService.checkAllMedicamentsForStockAlerts(medicaments, pharmacieId);

            // Compter les médicaments en rupture
            long stocksEpuises = medicaments.stream()
                    .filter(med -> med.getQuantite() != null && med.getQuantite() == 0)
                    .count();

            System.out.println("✅ Vérification terminée: " + stocksEpuises + " médicaments en rupture");

            redirectAttributes.addFlashAttribute("infoMessage", "Vérification des stocks terminée");
            return "redirect:/pharmacie/" + pharmacieId + "/gestion-stock";

        } catch (Exception e) {
            System.out.println("❌ Erreur vérification stocks: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Erreur lors de la vérification des stocks: " + e.getMessage());
            return "error";
        }
    }

    // Rechercher des médicaments
    @GetMapping("/rechercher")
    public String rechercherMedicaments(@PathVariable Long pharmacieId, @RequestParam String nom, Model model) {
        try {
            System.out.println("🔍 RECHERCHE: " + nom);

            // Récupérer l'utilisateur
            Utilisateur utilisateur = utilisateurRepository.findById(pharmacieId).orElse(null);
            if (utilisateur != null) {
                model.addAttribute("utilisateur", utilisateur);
            }

            List<Medpharmacie> medicaments = medpharmacieRepository.findByPharmacieIdAndNomContainingIgnoreCase(pharmacieId, nom);
            model.addAttribute("medicaments", medicaments);
            model.addAttribute("termerecherche", nom);
            model.addAttribute("pharmacieId", pharmacieId);

            System.out.println("✅ " + medicaments.size() + " résultat(s) trouvé(s)");

            return "pharmacie/gestion-stock";

        } catch (Exception e) {
            System.out.println("❌ Erreur recherche: " + e.getMessage());
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
            int count = notificationService.getUnreadCount(pharmacieId);
            System.out.println("📊 API Notification count: " + count);
            return count;
        } catch (Exception e) {
            System.out.println("❌ Erreur API notifications: " + e.getMessage());
            return 0;
        }
    }

    // TEST METHOD - Pour vérifier que le contrôleur fonctionne
    @GetMapping("/test")
    @ResponseBody
    public String test(@PathVariable Long pharmacieId) {
        return "✅ StockController fonctionnel pour pharmacie ID: " + pharmacieId;
    }
}