package com.example.demo.controller;

import com.example.demo.model.Commande;
import com.example.demo.model.Utilisateur;
import com.example.demo.repository.CommandeRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/confirmation")
public class ConfirmationController {

    @Autowired
    private CommandeRepository commandeRepository;

    @GetMapping("/{commandeId}")
    public String showConfirmation(@PathVariable Long commandeId,
                                   HttpSession session,
                                   Model model) {
        try {
            Utilisateur utilisateur = (Utilisateur) session.getAttribute("utilisateurConnecte");
            if (utilisateur == null) {
                return "redirect:/login";
            }

            // Charger la commande avec les médicaments - Utilisez une jointure FETCH
            Optional<Commande> commandeOpt = commandeRepository.findByIdWithMedicaments(commandeId);
            if (commandeOpt.isEmpty()) {
                model.addAttribute("error", "Commande non trouvée");
                return "error";
            }

            Commande commande = commandeOpt.get();

            // Vérifier que la commande appartient à l'utilisateur
            if (!commande.getUtilisateurId().equals(utilisateur.getId())) {
                model.addAttribute("error", "Accès non autorisé");
                return "error";
            }

            // Vider le panier après confirmation
            viderPanierUtilisateur(session, commande.getPharmacieId());

            // Ajouter les données au modèle - NE PAS utiliser toutesCommandes pour l'affichage des produits
            model.addAttribute("commande", commande);
            model.addAttribute("total", commande.getPrixTotal());
            model.addAttribute("pharmacieId", commande.getPharmacieId());

            return "confirmation-commande";

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Erreur lors de l'affichage de la confirmation: " + e.getMessage());
            return "error";
        }
    }

    private void viderPanierUtilisateur(HttpSession session, Long pharmacieId) {
        session.setAttribute("viderPanier", true);
        session.setAttribute("pharmacieIdPanier", pharmacieId);
        session.removeAttribute("commande_medicamentIds");
        session.removeAttribute("commande_quantites");
        session.removeAttribute("commande_pharmacieId");
        session.removeAttribute("lastOrderId");
    }
}
