package com.example.demo.controller;

import com.example.demo.model.Commande;
import com.example.demo.model.Utilisateur;
import com.example.demo.service.CommandeService;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class CommandeController {

    private static final Logger logger = LoggerFactory.getLogger(CommandeController.class);

    @Autowired
    private CommandeService commandeService;

    // ======= PAGE DES COMMANDES =======
    @GetMapping("/commande-pharmacie")
    public String showCommandes(Model model, HttpSession session,
                                @RequestParam(value = "pharmacieId", required = false) Long pharmacieIdParam) {
        logger.info("📦 Accès à /commande-pharmacie");

        try {
            // Récupérer l'utilisateur connecté depuis la session
            Utilisateur utilisateur = (Utilisateur) session.getAttribute("utilisateurConnecte");

            if (utilisateur == null) {
                return "redirect:/login";
            }

            // AJOUT: Ajouter l'utilisateur et le nom de la pharmacie au modèle
            model.addAttribute("utilisateur", utilisateur);
            model.addAttribute("nomPharmacie", utilisateur.getNomPharmacie());
            model.addAttribute("pharmacie", utilisateur);

            // Déterminer la pharmacie à afficher - utiliser une variable final
            final Long pharmacieId;

            if (pharmacieIdParam != null) {
                pharmacieId = pharmacieIdParam;
            } else {
                pharmacieId = utilisateur.getId();
            }

            // Charger TOUTES les commandes de cette pharmacie
            List<Commande> toutesCommandes = commandeService.findByPharmacieIdWithMedicaments(pharmacieId);

            if (toutesCommandes.isEmpty()) {
                logger.warn("⚠️ Aucune commande trouvée pour pharmacieId: {}. Tentative avec toutes les commandes...", pharmacieId);
                toutesCommandes = commandeService.findAll().stream()
                        .filter(c -> c.getPharmacieId() != null && c.getPharmacieId().equals(pharmacieId))
                        .collect(Collectors.toList());
            }

            // Séparer les commandes en attente et archivées
            List<Commande> commandesEnAttente = toutesCommandes.stream()
                    .filter(c -> c.getStatut() != null &&
                            !"VALIDEE".equals(c.getStatut()) &&
                            !"ANNULEE".equals(c.getStatut()))
                    .collect(Collectors.toList());

            List<Commande> commandesArchivees = toutesCommandes.stream()
                    .filter(c -> c.getStatut() != null &&
                            ("VALIDEE".equals(c.getStatut()) || "ANNULEE".equals(c.getStatut())))
                    .collect(Collectors.toList());

            // DEBUG: Log des commandes
            logger.info("🔍 Toutes les commandes pour pharmacie {}: {}", pharmacieId, toutesCommandes.size());

            for (Commande cmd : toutesCommandes) {
                logger.info("   - Commande {}: pharmacieId={}, statut={}",
                        cmd.getId(), cmd.getPharmacieId(), cmd.getStatut());
            }

            model.addAttribute("commandesEnAttente", commandesEnAttente);
            model.addAttribute("commandesArchivees", commandesArchivees);

            // Info de débogage
            String debugInfo = String.format(
                    "Pharmacie: %s (ID: %d) | Commandes en attente: %d | Commandes archivées: %d | Total commandes: %d",
                    utilisateur.getNomPharmacie(), pharmacieId,
                    commandesEnAttente.size(), commandesArchivees.size(), toutesCommandes.size()
            );
            model.addAttribute("debugInfo", debugInfo);

            logger.info("✅ Affichage de {} commandes en attente et {} archivées pour la pharmacie: {}",
                    commandesEnAttente.size(), commandesArchivees.size(), utilisateur.getNomPharmacie());

            return "commande-pharmacie";

        } catch (Exception e) {
            logger.error("❌ Erreur lors du chargement des commandes: {}", e.getMessage(), e);
            model.addAttribute("error", "Erreur lors du chargement des commandes: " + e.getMessage());
            return "commande-pharmacie";
        }
    }

    // ======= VALIDER UNE COMMANDE =======
    @PostMapping("/commande-pharmacie/{id}/valider")
    public String validerCommande(@PathVariable Long id, HttpSession session, Model model) {
        try {
            // Récupérer l'utilisateur connecté depuis la session
            Utilisateur utilisateur = (Utilisateur) session.getAttribute("utilisateurConnecte");

            if (utilisateur == null) {
                return "redirect:/login";
            }

            // AJOUT: Ajouter l'utilisateur et le nom de la pharmacie au modèle
            model.addAttribute("utilisateur", utilisateur);
            model.addAttribute("nomPharmacie", utilisateur.getNomPharmacie());

            Long pharmacieId = utilisateur.getId();

            // Valider la commande
            Commande commande = commandeService.findByIdAndPharmacieId(id, pharmacieId)
                    .orElseThrow(() -> new RuntimeException("Commande non trouvée"));

            commande.setStatut("VALIDEE");
            commandeService.save(commande);

            logger.info("✅ Commande {} validée par la pharmacie {}", id, pharmacieId);

        } catch (Exception e) {
            logger.error("❌ Erreur validation: {}", e.getMessage());
        }

        return "redirect:/commande-pharmacie";
    }

    // ======= ANNULER UNE COMMANDE =======
    @PostMapping("/commande-pharmacie/{id}/annuler")
    public String annulerCommande(@PathVariable Long id, HttpSession session, Model model) {
        try {
            // Récupérer l'utilisateur connecté depuis la session
            Utilisateur utilisateur = (Utilisateur) session.getAttribute("utilisateurConnecte");

            if (utilisateur == null) {
                return "redirect:/login";
            }

            // AJOUT: Ajouter l'utilisateur et le nom de la pharmacie au modèle
            model.addAttribute("utilisateur", utilisateur);
            model.addAttribute("nomPharmacie", utilisateur.getNomPharmacie());

            Long pharmacieId = utilisateur.getId();

            // Annuler la commande
            Commande commande = commandeService.findByIdAndPharmacieId(id, pharmacieId)
                    .orElseThrow(() -> new RuntimeException("Commande non trouvée"));

            commande.setStatut("ANNULEE");
            commandeService.save(commande);

            logger.info("✅ Commande {} annulée par la pharmacie {}", id, pharmacieId);

        } catch (Exception e) {
            logger.error("❌ Erreur annulation: {}", e.getMessage());
        }

        return "redirect:/commande-pharmacie";
    }

    // ======= CRÉER UNE COMMANDE DE TEST =======
    @GetMapping("/commande-test")
    public String creerCommandeTest(HttpSession session, Model model) {
        try {
            // Récupérer l'utilisateur connecté depuis la session
            Utilisateur utilisateur = (Utilisateur) session.getAttribute("utilisateurConnecte");

            if (utilisateur == null) {
                return "redirect:/login";
            }

            // AJOUT: Ajouter l'utilisateur et le nom de la pharmacie au modèle
            model.addAttribute("utilisateur", utilisateur);
            model.addAttribute("nomPharmacie", utilisateur.getNomPharmacie());

            Long pharmacieId = utilisateur.getId();

            Commande commandeTest = new Commande();
            commandeTest.setPharmacieId(pharmacieId);
            commandeTest.setStatut("EN_ATTENTE");
            commandeTest.setPrixTotal(99.99);
            commandeTest.setNom("Test");
            commandeTest.setPrenom("Utilisateur");
            commandeTest.setUtilisateurId(1L); // ID de l'utilisateur test

            commandeService.save(commandeTest);
            logger.info("✅ Commande de test créée avec ID: {} pour pharmacie ID: {}",
                    commandeTest.getId(), pharmacieId);

            return "redirect:/commande-pharmacie?test=success";
        } catch (Exception e) {
            logger.error("❌ Erreur création commande test: {}", e.getMessage());
            return "redirect:/commande-pharmacie?test=error";
        }
    }
}