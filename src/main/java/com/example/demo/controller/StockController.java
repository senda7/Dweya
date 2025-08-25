package com.example.demo.controller;

import com.example.demo.model.Medpharmacie;
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

    // Méthode pour obtenir l'ID de la pharmacie (version temporaire)
    private Long getPharmacieId() {
        // TEMPORAIRE: Retourne un ID fixe pour le développement
        // À REMPLACER par votre logique d'authentification réelle
        return 1L;
    }

    // Page principale de gestion du stock
    @GetMapping
    public String afficherGestionStock(Model model) {
        Long pharmacieId = getPharmacieId();
        List<Medpharmacie> medicaments = medpharmacieService.getMedicamentsByPharmacie(pharmacieId);

        model.addAttribute("medicaments", medicaments);
        model.addAttribute("totalMedicaments", medicaments != null ? medicaments.size() : 0);
        return "pharmacie/gestion-stock";
    }

    // Afficher le formulaire d'ajout
    @GetMapping("/ajouter")
    public String afficherFormulaireAjout(Model model) {
        Medpharmacie medicament = new Medpharmacie();
        medicament.setPharmacieId(getPharmacieId()); // Définir l'ID pharmacie
        model.addAttribute("medicament", medicament);
        return "pharmacie/ajouter";
    }

    // Traiter l'ajout d'un médicament
    @PostMapping("/ajouter")
    public String ajouterMedicament(@ModelAttribute Medpharmacie medicament,
                                    @RequestParam("photo") MultipartFile file,
                                    RedirectAttributes redirectAttributes) {
        try {
            // Validation basique
            if (file.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Veuillez sélectionner une photo");
                return "redirect:/stock/ajouter";
            }

            // Vérifier la taille du fichier (max 5MB)
            if (file.getSize() > 5 * 1024 * 1024) {
                redirectAttributes.addFlashAttribute("error", "La photo ne doit pas dépasser 5MB");
                return "redirect:/stock/ajouter";
            }

            // Vérifier le type de fichier
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                redirectAttributes.addFlashAttribute("error", "Veuillez sélectionner une image valide");
                return "redirect:/stock/ajouter";
            }

            // Convertir l'image en Base64 et l'enregistrer
            String imageData = convertToBase64(file);
            medicament.setPhotoData(imageData);
            medicament.setPharmacieId(getPharmacieId());

            // Sauvegarder le médicament
            medpharmacieService.saveMedicament(medicament);

            redirectAttributes.addFlashAttribute("success", "Médicament ajouté avec succès!");

        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors du traitement de l'image: " + e.getMessage());
            return "redirect:/stock/ajouter";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de l'ajout: " + e.getMessage());
            return "redirect:/stock/ajouter";
        }

        return "redirect:/stock";
    }

    // Afficher le formulaire de modification
    @GetMapping("/modifier/{id}")
    public String afficherFormulaireModification(@PathVariable Long id, Model model) {
        Long pharmacieId = getPharmacieId();
        Optional<Medpharmacie> medicamentOptional = medpharmacieService.getMedicamentById(id);

        if (medicamentOptional.isEmpty()) {
            return "redirect:/stock";
        }

        Medpharmacie medicament = medicamentOptional.get();

        // Vérifier que le médicament appartient à la pharmacie
        if (!medicament.getPharmacieId().equals(pharmacieId)) {
            return "redirect:/stock";
        }

        model.addAttribute("medicament", medicament);
        return "pharmacie/modifier";
    }

    // Traiter la modification d'un médicament
    @PostMapping("/modifier/{id}")
    public String modifierMedicament(@PathVariable Long id,
                                     @ModelAttribute Medpharmacie medicament,
                                     @RequestParam(value = "photo", required = false) MultipartFile file,
                                     RedirectAttributes redirectAttributes) {
        try {
            Long pharmacieId = getPharmacieId();

            // Récupérer le médicament existant
            Optional<Medpharmacie> existingMedicamentOptional = medpharmacieService.getMedicamentById(id);
            if (existingMedicamentOptional.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Médicament non trouvé");
                return "redirect:/stock";
            }

            Medpharmacie existingMedicament = existingMedicamentOptional.get();

            // Vérifier les permissions
            if (!existingMedicament.getPharmacieId().equals(pharmacieId)) {
                redirectAttributes.addFlashAttribute("error", "Accès non autorisé");
                return "redirect:/stock";
            }

            // Gestion de la photo
            if (file != null && !file.isEmpty()) {
                // Validation de la nouvelle photo
                if (file.getSize() > 5 * 1024 * 1024) {
                    redirectAttributes.addFlashAttribute("error", "La photo ne doit pas dépasser 5MB");
                    return "redirect:/stock/modifier/" + id;
                }

                String contentType = file.getContentType();
                if (contentType == null || !contentType.startsWith("image/")) {
                    redirectAttributes.addFlashAttribute("error", "Veuillez sélectionner une image valide");
                    return "redirect:/stock/modifier/" + id;
                }

                // Convertir et enregistrer la nouvelle photo
                String imageData = convertToBase64(file);
                medicament.setPhotoData(imageData);
            } else {
                // Conserver l'ancienne photo
                medicament.setPhotoData(existingMedicament.getPhotoData());
            }

            // Mettre à jour le médicament
            medicament.setId(id);
            medicament.setPharmacieId(pharmacieId);
            medpharmacieService.saveMedicament(medicament);

            redirectAttributes.addFlashAttribute("success", "Médicament modifié avec succès!");

        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors du traitement de l'image: " + e.getMessage());
            return "redirect:/stock/modifier/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la modification: " + e.getMessage());
            return "redirect:/stock/modifier/" + id;
        }

        return "redirect:/stock";
    }

    // Supprimer un médicament
    @GetMapping("/supprimer/{id}")
    public String supprimerMedicament(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Long pharmacieId = getPharmacieId();
            Optional<Medpharmacie> medicamentOptional = medpharmacieService.getMedicamentById(id);

            if (medicamentOptional.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Médicament non trouvé");
                return "redirect:/stock";
            }

            Medpharmacie medicament = medicamentOptional.get();

            // Vérifier les permissions
            if (!medicament.getPharmacieId().equals(pharmacieId)) {
                redirectAttributes.addFlashAttribute("error", "Accès non autorisé");
                return "redirect:/stock";
            }

            // Supprimer le médicament (la photo sera automatiquement supprimée avec lui)
            medpharmacieService.deleteMedicament(id);
            redirectAttributes.addFlashAttribute("success", "Médicament supprimé avec succès!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la suppression: " + e.getMessage());
        }

        return "redirect:/stock";
    }

    // Rechercher des médicaments
    @GetMapping("/rechercher")
    public String rechercherMedicaments(@RequestParam String nom, Model model) {
        Long pharmacieId = getPharmacieId();
        List<Medpharmacie> medicaments = medpharmacieService.searchMedicamentsByPharmacie(pharmacieId, nom);

        model.addAttribute("medicaments", medicaments);
        model.addAttribute("totalMedicaments", medicaments != null ? medicaments.size() : 0);
        model.addAttribute("termerecherche", nom);

        return "pharmacie/gestion-stock";
    }

    // Méthode utilitaire pour convertir une image en Base64
    private String convertToBase64(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }

        String mimeType = file.getContentType();
        if (mimeType == null) {
            mimeType = "image/jpeg"; // Type par défaut
        }

        byte[] bytes = file.getBytes();
        String base64Image = Base64.getEncoder().encodeToString(bytes);

        return "data:" + mimeType + ";base64," + base64Image;
    }
}
