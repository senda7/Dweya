package com.example.demo.controller;

import com.example.demo.model.Medpharmacie;
import com.example.demo.model.Utilisateur;
import com.example.demo.service.MedpharmacieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/stock")
public class StockController {

    @Autowired
    private MedpharmacieService medpharmacieService;

    // 🔹 Récupérer la pharmacie connectée
    private Utilisateur getPharmacieConnectee() {
        // À remplacer par Spring Security ou ton système d'authentification réel
        Utilisateur pharmacie = new Utilisateur();
        pharmacie.setId(1L); // Exemple temporaire
        return pharmacie;
    }

    // 🔹 Affichage de tous les médicaments de la pharmacie connectée
    @GetMapping
    public String afficherGestionStock(Model model) {
        Utilisateur pharmacie = getPharmacieConnectee();
        List<Medpharmacie> medicaments = medpharmacieService.getMedicamentsByPharmacie(pharmacie);

        model.addAttribute("medicaments", medicaments);
        model.addAttribute("totalMedicaments", medicaments.size());
        return "pharmacie/gestion-stock";
    }

    // 🔹 Formulaire d'ajout
    @GetMapping("/ajouter")
    public String afficherFormulaireAjout(Model model) {
        Medpharmacie medicament = new Medpharmacie();
        model.addAttribute("medicament", medicament);
        return "pharmacie/ajouter";
    }

    // 🔹 Ajouter un médicament
    @PostMapping("/ajouter")
    public String ajouterMedicament(@ModelAttribute Medpharmacie medicament,
                                    @RequestParam("photo") MultipartFile file,
                                    RedirectAttributes redirectAttributes) {
        try {
            Utilisateur pharmacie = getPharmacieConnectee();
            String photoData = validateAndConvertPhoto(file, redirectAttributes);
            if (photoData == null) return "redirect:/stock/ajouter";

            medicament.setPhotoData(photoData);
            medicament.setPharmacie(pharmacie);
            medpharmacieService.saveMedicament(medicament);

            redirectAttributes.addFlashAttribute("success", "Médicament ajouté avec succès!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de l'ajout: " + e.getMessage());
        }
        return "redirect:/stock";
    }

    // 🔹 Formulaire modification
    @GetMapping("/modifier/{id}")
    public String afficherFormulaireModification(@PathVariable Long id, Model model,
                                                 RedirectAttributes redirectAttributes) {
        Utilisateur pharmacie = getPharmacieConnectee();
        Optional<Medpharmacie> medicamentOpt = medpharmacieService.getMedicamentByIdAndPharmacie(id, pharmacie);

        if (medicamentOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Médicament non trouvé ou accès non autorisé");
            return "redirect:/stock";
        }

        model.addAttribute("medicament", medicamentOpt.get());
        return "pharmacie/modifier";
    }

    // 🔹 Modifier un médicament
    @PostMapping("/modifier/{id}")
    public String modifierMedicament(@PathVariable Long id,
                                     @ModelAttribute Medpharmacie medicament,
                                     @RequestParam(value = "photo", required = false) MultipartFile file,
                                     RedirectAttributes redirectAttributes) {
        try {
            Utilisateur pharmacie = getPharmacieConnectee();
            Optional<Medpharmacie> existingOpt = medpharmacieService.getMedicamentByIdAndPharmacie(id, pharmacie);

            if (existingOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Médicament non trouvé ou accès non autorisé");
                return "redirect:/stock";
            }

            Medpharmacie existing = existingOpt.get();

            if (file != null && !file.isEmpty()) {
                String photoData = validateAndConvertPhoto(file, redirectAttributes);
                if (photoData == null) return "redirect:/stock/modifier/" + id;
                medicament.setPhotoData(photoData);
            } else {
                medicament.setPhotoData(existing.getPhotoData());
            }

            medicament.setId(id);
            medicament.setPharmacie(pharmacie);
            medpharmacieService.saveMedicament(medicament);

            redirectAttributes.addFlashAttribute("success", "Médicament modifié avec succès!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la modification: " + e.getMessage());
        }
        return "redirect:/stock";
    }

    // 🔹 Supprimer un médicament
    @GetMapping("/supprimer/{id}")
    public String supprimerMedicament(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Utilisateur pharmacie = getPharmacieConnectee();
            Optional<Medpharmacie> medicamentOpt = medpharmacieService.getMedicamentByIdAndPharmacie(id, pharmacie);

            if (medicamentOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Médicament non trouvé ou accès non autorisé");
                return "redirect:/stock";
            }

            medpharmacieService.deleteMedicament(medicamentOpt.get());
            redirectAttributes.addFlashAttribute("success", "Médicament supprimé avec succès!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la suppression: " + e.getMessage());
        }
        return "redirect:/stock";
    }

    // 🔹 Rechercher un médicament
    @GetMapping("/rechercher")
    public String rechercherMedicaments(@RequestParam String nom, Model model) {
        Utilisateur pharmacie = getPharmacieConnectee();
        List<Medpharmacie> medicaments = medpharmacieService.searchMedicamentsByPharmacie(pharmacie, nom);

        model.addAttribute("medicaments", medicaments);
        model.addAttribute("totalMedicaments", medicaments.size());
        model.addAttribute("termerecherche", nom);

        return "pharmacie/gestion-stock";
    }

    // 🔹 Validation et conversion photo en Base64
    private String validateAndConvertPhoto(MultipartFile file, RedirectAttributes redirectAttributes) throws IOException {
        if (file == null || file.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Veuillez sélectionner une photo");
            return null;
        }
        if (file.getSize() > 5 * 1024 * 1024) {
            redirectAttributes.addFlashAttribute("error", "La photo ne doit pas dépasser 5MB");
            return null;
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            redirectAttributes.addFlashAttribute("error", "Veuillez sélectionner une image valide");
            return null;
        }

        byte[] bytes = file.getBytes();
        return "data:" + contentType + ";base64," + Base64.getEncoder().encodeToString(bytes);
    }
}
