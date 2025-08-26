package com.example.demo.controller;

import com.example.demo.model.Medpharmacie;
import com.example.demo.model.Utilisateur;
import com.example.demo.service.MedpharmacieService;
import jakarta.servlet.http.HttpSession;
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

    // ===== Récupérer l'ID pharmacie depuis la session =====
    private Long getPharmacieId(HttpSession session) {
        Utilisateur utilisateur = (Utilisateur) session.getAttribute("utilisateurConnecte");
        if (utilisateur != null && utilisateur.getTypeUtilisateur() == Utilisateur.TypeUtilisateur.PHARMACIE) {
            return utilisateur.getId();
        }
        return null;
    }

    // ===== Page principale de gestion du stock =====
    @GetMapping
    public String afficherGestionStock(HttpSession session, Model model) {
        Long pharmacieId = getPharmacieId(session);
        if (pharmacieId == null) return "redirect:/login";

        List<Medpharmacie> medicaments = medpharmacieService.getMedicamentsByPharmacie(pharmacieId);
        model.addAttribute("medicaments", medicaments);
        model.addAttribute("totalMedicaments", medicaments != null ? medicaments.size() : 0);
        return "pharmacie/gestion-stock";
    }

    // ===== Afficher le formulaire d'ajout =====
    @GetMapping("/ajouter")
    public String afficherFormulaireAjout(HttpSession session, Model model) {
        Long pharmacieId = getPharmacieId(session);
        if (pharmacieId == null) return "redirect:/login";

        Medpharmacie medicament = new Medpharmacie();
        medicament.setPharmacieId(pharmacieId);
        model.addAttribute("medicament", medicament);
        return "pharmacie/ajouter";
    }

    // ===== Ajouter un médicament =====
    @PostMapping("/ajouter")
    public String ajouterMedicament(@ModelAttribute Medpharmacie medicament,
                                    @RequestParam("photo") MultipartFile file,
                                    HttpSession session,
                                    RedirectAttributes redirectAttributes) {
        try {
            Long pharmacieId = getPharmacieId(session);
            if (pharmacieId == null) return "redirect:/login";

            if (file.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Veuillez sélectionner une photo");
                return "redirect:/stock/ajouter";
            }

            if (file.getSize() > 5 * 1024 * 1024) {
                redirectAttributes.addFlashAttribute("error", "La photo ne doit pas dépasser 5MB");
                return "redirect:/stock/ajouter";
            }

            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                redirectAttributes.addFlashAttribute("error", "Veuillez sélectionner une image valide");
                return "redirect:/stock/ajouter";
            }

            medicament.setPhotoData(convertToBase64(file));
            medicament.setPharmacieId(pharmacieId);
            medpharmacieService.saveMedicament(medicament);
            redirectAttributes.addFlashAttribute("success", "Médicament ajouté avec succès!");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors du traitement de l'image: " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de l'ajout: " + e.getMessage());
        }

        return "redirect:/stock";
    }

    // ===== Afficher le formulaire de modification =====
    @GetMapping("/modifier/{id}")
    public String afficherFormulaireModification(@PathVariable Long id, HttpSession session, Model model) {
        Long pharmacieId = getPharmacieId(session);
        if (pharmacieId == null) return "redirect:/login";

        Optional<Medpharmacie> medicamentOptional = medpharmacieService.getMedicamentById(id);
        if (medicamentOptional.isEmpty()) return "redirect:/stock";

        Medpharmacie medicament = medicamentOptional.get();
        if (!medicament.getPharmacieId().equals(pharmacieId)) return "redirect:/stock";

        model.addAttribute("medicament", medicament);
        return "pharmacie/modifier";
    }

    // ===== Modifier un médicament =====
    @PostMapping("/modifier/{id}")
    public String modifierMedicament(@PathVariable Long id,
                                     @ModelAttribute Medpharmacie medicament,
                                     @RequestParam(value = "photo", required = false) MultipartFile file,
                                     HttpSession session,
                                     RedirectAttributes redirectAttributes) {
        try {
            Long pharmacieId = getPharmacieId(session);
            if (pharmacieId == null) return "redirect:/login";

            Optional<Medpharmacie> existingMedicamentOptional = medpharmacieService.getMedicamentById(id);
            if (existingMedicamentOptional.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Médicament non trouvé");
                return "redirect:/stock";
            }

            Medpharmacie existingMedicament = existingMedicamentOptional.get();
            if (!existingMedicament.getPharmacieId().equals(pharmacieId)) {
                redirectAttributes.addFlashAttribute("error", "Accès non autorisé");
                return "redirect:/stock";
            }

            if (file != null && !file.isEmpty()) {
                if (file.getSize() > 5 * 1024 * 1024) {
                    redirectAttributes.addFlashAttribute("error", "La photo ne doit pas dépasser 5MB");
                    return "redirect:/stock/modifier/" + id;
                }
                String contentType = file.getContentType();
                if (contentType == null || !contentType.startsWith("image/")) {
                    redirectAttributes.addFlashAttribute("error", "Veuillez sélectionner une image valide");
                    return "redirect:/stock/modifier/" + id;
                }
                medicament.setPhotoData(convertToBase64(file));
            } else {
                medicament.setPhotoData(existingMedicament.getPhotoData());
            }

            medicament.setId(id);
            medicament.setPharmacieId(pharmacieId);
            medpharmacieService.saveMedicament(medicament);
            redirectAttributes.addFlashAttribute("success", "Médicament modifié avec succès!");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors du traitement de l'image: " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la modification: " + e.getMessage());
        }

        return "redirect:/stock";
    }

    // ===== Supprimer un médicament =====
    @GetMapping("/supprimer/{id}")
    public String supprimerMedicament(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        try {
            Long pharmacieId = getPharmacieId(session);
            if (pharmacieId == null) return "redirect:/login";

            Optional<Medpharmacie> medicamentOptional = medpharmacieService.getMedicamentById(id);
            if (medicamentOptional.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Médicament non trouvé");
                return "redirect:/stock";
            }

            Medpharmacie medicament = medicamentOptional.get();
            if (!medicament.getPharmacieId().equals(pharmacieId)) {
                redirectAttributes.addFlashAttribute("error", "Accès non autorisé");
                return "redirect:/stock";
            }

            medpharmacieService.deleteMedicament(id);
            redirectAttributes.addFlashAttribute("success", "Médicament supprimé avec succès!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la suppression: " + e.getMessage());
        }

        return "redirect:/stock";
    }

    // ===== Rechercher des médicaments =====
    @GetMapping("/rechercher")
    public String rechercherMedicaments(@RequestParam String nom, HttpSession session, Model model) {
        Long pharmacieId = getPharmacieId(session);
        if (pharmacieId == null) return "redirect:/login";

        List<Medpharmacie> medicaments = medpharmacieService.searchMedicamentsByPharmacie(pharmacieId, nom);
        model.addAttribute("medicaments", medicaments);
        model.addAttribute("totalMedicaments", medicaments != null ? medicaments.size() : 0);
        model.addAttribute("termerecherche", nom);

        return "pharmacie/gestion-stock";
    }

    // ===== Convertir une image en Base64 =====
    private String convertToBase64(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) return null;

        String mimeType = file.getContentType();
        if (mimeType == null) mimeType = "image/jpeg";

        byte[] bytes = file.getBytes();
        String base64Image = Base64.getEncoder().encodeToString(bytes);

        return "data:" + mimeType + ";base64," + base64Image;
    }
}
