package com.example.demo.controller;

import com.example.demo.model.Medpharmacie;
import com.example.demo.model.Utilisateur;
import com.example.demo.repository.MedpharmacieRepository;
import com.example.demo.repository.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;
import java.util.Optional;

@Controller
public class PharmacieStockController {

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private MedpharmacieRepository medpharmacieRepository;

    // REMPLACER le mapping en double par un seul chemin
    @GetMapping("/stock/pharmacie/{id}") // ← Supprimer "/pharmacie/{id}/stock"
    public String showPharmacyStock(@PathVariable Long id,
                                    @RequestParam(required = false) String search,
                                    Model model) {
        try {
            // Récupérer la pharmacie par son ID
            Optional<Utilisateur> pharmacieOpt = utilisateurRepository.findById(id);

            if (pharmacieOpt.isPresent()) {
                Utilisateur pharmacie = pharmacieOpt.get();

                // Vérifier que c'est bien une pharmacie
                if (pharmacie.getTypeUtilisateur() == Utilisateur.TypeUtilisateur.PHARMACIE) {
                    // Récupérer les médicaments de cette pharmacie
                    List<Medpharmacie> medicaments;

                    if (search != null && !search.isEmpty()) {
                        // Recherche par nom de médicament
                        medicaments = medpharmacieRepository.findByPharmacieIdAndNomContainingIgnoreCase(id, search);
                    } else {
                        medicaments = medpharmacieRepository.findByPharmacieId(id);
                    }

                    model.addAttribute("pharmacie", pharmacie);
                    model.addAttribute("medicaments", medicaments);
                    model.addAttribute("searchTerm", search);
                    return "stock-pharmacie"; // Retourne le bon template
                }
            }

            // Si la pharmacie n'est pas trouvée
            model.addAttribute("error", "Pharmacie non trouvée");
            return "error";

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Erreur lors du chargement du stock: " + e.getMessage());
            return "error";
        }
    }
}