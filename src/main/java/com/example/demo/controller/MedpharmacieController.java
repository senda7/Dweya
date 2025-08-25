package com.example.demo.controller;

import com.example.demo.model.Medpharmacie;
import com.example.demo.model.Utilisateur;
import com.example.demo.service.MedpharmacieService;
import com.example.demo.service.UtilisateurService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/stock")
public class MedpharmacieController {

    @Autowired
    private MedpharmacieService medpharmacieService;

    @Autowired
    private UtilisateurService utilisateurService; // pour récupérer la pharmacie

    // 🔹 Récupérer tous les médicaments d’une pharmacie
    @GetMapping("/pharmacie/{pharmacieId}")
    public List<Medpharmacie> getMedicamentsByPharmacie(@PathVariable Long pharmacieId) {
        Utilisateur pharmacie = utilisateurService.getUtilisateurById(pharmacieId)
                .orElseThrow(() -> new RuntimeException("Pharmacie non trouvée"));
        return medpharmacieService.getMedicamentsByPharmacie(pharmacie);
    }

    // 🔹 Rechercher un médicament par nom dans une pharmacie
    @GetMapping("/pharmacie/{pharmacieId}/search")
    public List<Medpharmacie> searchMedicamentsByPharmacie(
            @PathVariable Long pharmacieId,
            @RequestParam String nom) {
        Utilisateur pharmacie = utilisateurService.getUtilisateurById(pharmacieId)
                .orElseThrow(() -> new RuntimeException("Pharmacie non trouvée"));
        return medpharmacieService.searchMedicamentsByPharmacie(pharmacie, nom);
    }

    // 🔹 Récupérer un médicament par ID et pharmacie
    @GetMapping("/pharmacie/{pharmacieId}/{id}")
    public Medpharmacie getMedicamentById(
            @PathVariable Long pharmacieId,
            @PathVariable Long id) {
        Utilisateur pharmacie = utilisateurService.getUtilisateurById(pharmacieId)
                .orElseThrow(() -> new RuntimeException("Pharmacie non trouvée"));
        return medpharmacieService.getMedicamentByIdAndPharmacie(id, pharmacie)
                .orElseThrow(() -> new RuntimeException("Médicament non trouvé pour cette pharmacie"));
    }

    // 🔹 Ajouter un médicament
    @PostMapping("/pharmacie/{pharmacieId}")
    public Medpharmacie addMedicament(
            @PathVariable Long pharmacieId,
            @RequestBody Medpharmacie medicament) {
        Utilisateur pharmacie = utilisateurService.getUtilisateurById(pharmacieId)
                .orElseThrow(() -> new RuntimeException("Pharmacie non trouvée"));
        medicament.setPharmacie(pharmacie);
        return medpharmacieService.saveMedicament(medicament);
    }

    // 🔹 Modifier un médicament
    @PutMapping("/pharmacie/{pharmacieId}/{id}")
    public Medpharmacie updateMedicament(
            @PathVariable Long pharmacieId,
            @PathVariable Long id,
            @RequestBody Medpharmacie medicament) {
        Utilisateur pharmacie = utilisateurService.getUtilisateurById(pharmacieId)
                .orElseThrow(() -> new RuntimeException("Pharmacie non trouvée"));
        Medpharmacie existing = medpharmacieService.getMedicamentByIdAndPharmacie(id, pharmacie)
                .orElseThrow(() -> new RuntimeException("Médicament non trouvé pour cette pharmacie"));
        medicament.setId(existing.getId());
        medicament.setPharmacie(pharmacie);
        return medpharmacieService.saveMedicament(medicament);
    }

    // 🔹 Supprimer un médicament
    @DeleteMapping("/pharmacie/{pharmacieId}/{id}")
    public void deleteMedicament(
            @PathVariable Long pharmacieId,
            @PathVariable Long id) {
        Utilisateur pharmacie = utilisateurService.getUtilisateurById(pharmacieId)
                .orElseThrow(() -> new RuntimeException("Pharmacie non trouvée"));
        Medpharmacie existing = medpharmacieService.getMedicamentByIdAndPharmacie(id, pharmacie)
                .orElseThrow(() -> new RuntimeException("Médicament non trouvé pour cette pharmacie"));
        medpharmacieService.deleteMedicament(existing);
    }
}

