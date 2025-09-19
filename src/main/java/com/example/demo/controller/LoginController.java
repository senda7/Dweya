package com.example.demo.controller;

import com.example.demo.model.Notification;
import com.example.demo.model.Role;
import com.example.demo.model.Utilisateur;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UtilisateurRepository;
import com.example.demo.service.AuthService;
import com.example.demo.service.NotificationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
public class LoginController {

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private AuthService authService;

    @Autowired
    private NotificationService notificationService;

    // ===== LOGIN =====
    @GetMapping("/login")
    public String loginForm(Model model) {
        model.addAttribute("utilisateur", new Utilisateur());
        return "login";
    }

    @PostMapping("/login")
    public String loginSubmit(@RequestParam(required = false) String email,
                              @RequestParam(required = false) String motDePasse,
                              @ModelAttribute Utilisateur utilisateur,
                              Model model,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {

        Utilisateur u = null;

        if (email != null && motDePasse != null) {
            u = authService.login(email, motDePasse);
        } else if (utilisateur.getEmail() != null && utilisateur.getMotDePasse() != null) {
            Optional<Utilisateur> utilisateurOpt = utilisateurRepository.findFirstByEmailAndMotDePasse(
                    utilisateur.getEmail(), utilisateur.getMotDePasse());
            u = utilisateurOpt.orElse(null);
        }

        if (u != null && u.isEtat()) {
            session.setAttribute("userId", u.getId());
            session.setAttribute("role", u.getRole().getNom());
            session.setAttribute("utilisateurConnecte", u);

            switch (u.getTypeUtilisateur()) {
                case ADMIN: return "redirect:/accueil-admin";
                case PHARMACIE: return "redirect:/accueil-pharmacie";
                case UTILISATEUR:
                default: return "redirect:/accueil-utilisateur";
            }
        } else {
            redirectAttributes.addFlashAttribute("error", "Email ou mot de passe incorrect ou compte inactif !");
            return "redirect:/login";
        }
    }

    // ===== LOGOUT =====
    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    // ===== REGISTER =====
    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("utilisateur", new Utilisateur());
        model.addAttribute("roles", roleRepository.findAll());
        return "register";
    }

    @PostMapping("/register")
    public String registerSubmit(
            @ModelAttribute Utilisateur utilisateur,
            @RequestParam(value = "roleId", required = false) Long roleId,
            Model model
    ) {
        if (roleId == null) {
            model.addAttribute("erreur", "Veuillez sélectionner un rôle.");
            model.addAttribute("roles", roleRepository.findAll());
            return "register";
        }

        Role selectedRole = roleRepository.findById(roleId).orElse(null);
        if (selectedRole == null) {
            model.addAttribute("erreur", "Rôle invalide.");
            model.addAttribute("roles", roleRepository.findAll());
            return "register";
        }

        utilisateur.setRole(selectedRole);
        String roleName = selectedRole.getNom().toLowerCase();

        try {
            if ("pharmacie".equals(roleName)) {
                utilisateur.setTypeUtilisateur(Utilisateur.TypeUtilisateur.PHARMACIE);
                utilisateur.setEtat(false);

                if (utilisateur.getRegistreCommerceFile() != null && !utilisateur.getRegistreCommerceFile().isEmpty()) {
                    utilisateur.setRegistreCommerce(utilisateur.getRegistreCommerceFile().getBytes());
                }
                if (utilisateur.getCinPharmacienFile() != null && !utilisateur.getCinPharmacienFile().isEmpty()) {
                    utilisateur.setCinPharmacien(utilisateur.getCinPharmacienFile().getBytes());
                }
                if (utilisateur.getAutorisationMinistereFile() != null && !utilisateur.getAutorisationMinistereFile().isEmpty()) {
                    utilisateur.setAutorisationMinistere(utilisateur.getAutorisationMinistereFile().getBytes());
                }
            } else {
                utilisateur.setTypeUtilisateur(Utilisateur.TypeUtilisateur.UTILISATEUR);
                utilisateur.setEtat(true);
            }
        } catch (IOException e) {
            e.printStackTrace();
            model.addAttribute("erreur", "Erreur lors du traitement des fichiers.");
            model.addAttribute("roles", roleRepository.findAll());
            return "register";
        }

        if (utilisateurRepository.existsByEmail(utilisateur.getEmail())) {
            model.addAttribute("erreur", "Cet email est déjà utilisé.");
            model.addAttribute("roles", roleRepository.findAll());
            return "register";
        }

        utilisateurRepository.save(utilisateur);

        if ("pharmacie".equals(roleName)) {
            model.addAttribute("message", "Votre compte a été créé. En attente de validation.");
        } else {
            model.addAttribute("message", "Votre compte a été créé. Veuillez vous connecter.");
        }

        return "login";
    }

    // ===== PROFILS =====
    @GetMapping("/profil-utilisateur")
    public String profilUtilisateur(Model model, HttpSession session) {
        Utilisateur utilisateur = getUtilisateurSession(session);
        if (utilisateur == null) return "redirect:/login";
        model.addAttribute("utilisateur", utilisateur);
        return "utilisateur/profil-utilisateur";
    }

    @GetMapping("/profil-admin")
    public String profilAdmin(Model model, HttpSession session) {
        Utilisateur utilisateur = getUtilisateurSession(session);
        if (utilisateur == null) return "redirect:/login";
        model.addAttribute("utilisateur", utilisateur);
        return "admin/profil-admin";
    }

    @GetMapping("/profil-pharmacie")
    public String profilPharmacie(Model model, HttpSession session) {
        Utilisateur pharmacie = getUtilisateurSession(session);
        if (pharmacie == null) return "redirect:/login";

        model.addAttribute("utilisateur", pharmacie);

        // Notifications
        List<Notification> notifications = new ArrayList<>();
        int unreadCount = 0;
        try {
            notifications = notificationService.getAllNotifications(pharmacie.getId());
            unreadCount = notificationService.getUnreadCount(pharmacie.getId());
        } catch (Exception e) {
            System.out.println("Erreur notifications: " + e.getMessage());
        }

        model.addAttribute("notifications", notifications);
        model.addAttribute("unreadCount", unreadCount);

        return "pharmacie/profil-pharmacie";
    }

    // ===== MODIFIER PROFIL =====
    @PostMapping("/utilisateur/modifier")
    public String modifierUtilisateur(@ModelAttribute Utilisateur utilisateurModifie) {
        Utilisateur ancien = utilisateurRepository.findById(utilisateurModifie.getId()).orElse(null);
        if (ancien != null) {
            if (utilisateurModifie.getNom() != null && !utilisateurModifie.getNom().isEmpty()) ancien.setNom(utilisateurModifie.getNom());
            if (utilisateurModifie.getPrenom() != null && !utilisateurModifie.getPrenom().isEmpty()) ancien.setPrenom(utilisateurModifie.getPrenom());
            if (utilisateurModifie.getGenre() != null && !utilisateurModifie.getGenre().isEmpty()) ancien.setGenre(utilisateurModifie.getGenre());
            if (utilisateurModifie.getDateNaissance() != null) ancien.setDateNaissance(utilisateurModifie.getDateNaissance());
            if (utilisateurModifie.getEmail() != null && !utilisateurModifie.getEmail().isEmpty()) ancien.setEmail(utilisateurModifie.getEmail());
            if (utilisateurModifie.getTelephone() != null && !utilisateurModifie.getTelephone().isEmpty()) ancien.setTelephone(utilisateurModifie.getTelephone());
            if (utilisateurModifie.getAdresse() != null && !utilisateurModifie.getAdresse().isEmpty()) ancien.setAdresse(utilisateurModifie.getAdresse());
            if (utilisateurModifie.getVille() != null && !utilisateurModifie.getVille().isEmpty()) ancien.setVille(utilisateurModifie.getVille());
            utilisateurRepository.save(ancien);
        }
        return "redirect:/profil-utilisateur";
    }

    @PostMapping("/pharmacie/modifier")
    public String modifierPharmacie(
            @ModelAttribute Utilisateur pharmacieModifiee,
            @RequestParam(value = "registreCommerce", required = false) MultipartFile registreCommerceFile,
            @RequestParam(value = "cinPharmacien", required = false) MultipartFile cinPharmacienFile,
            @RequestParam(value = "autorisationMinistere", required = false) MultipartFile autorisationMinistereFile
    ) {
        Utilisateur ancienne = utilisateurRepository.findById(pharmacieModifiee.getId()).orElse(null);
        if (ancienne != null) {
            if (pharmacieModifiee.getNomPharmacie() != null && !pharmacieModifiee.getNomPharmacie().isEmpty())
                ancienne.setNomPharmacie(pharmacieModifiee.getNomPharmacie());
            if (pharmacieModifiee.getNom() != null && !pharmacieModifiee.getNom().isEmpty())
                ancienne.setNom(pharmacieModifiee.getNom());
            if (pharmacieModifiee.getPrenom() != null && !pharmacieModifiee.getPrenom().isEmpty())
                ancienne.setPrenom(pharmacieModifiee.getPrenom());
            if (pharmacieModifiee.getNumeroLicence() != null && !pharmacieModifiee.getNumeroLicence().isEmpty())
                ancienne.setNumeroLicence(pharmacieModifiee.getNumeroLicence());
            if (pharmacieModifiee.getNumeroOrdre() != null && !pharmacieModifiee.getNumeroOrdre().isEmpty())
                ancienne.setNumeroOrdre(pharmacieModifiee.getNumeroOrdre());
            if (pharmacieModifiee.getAdresse() != null && !pharmacieModifiee.getAdresse().isEmpty())
                ancienne.setAdresse(pharmacieModifiee.getAdresse());
            if (pharmacieModifiee.getVille() != null && !pharmacieModifiee.getVille().isEmpty())
                ancienne.setVille(pharmacieModifiee.getVille());
            if (pharmacieModifiee.getTelephone() != null && !pharmacieModifiee.getTelephone().isEmpty())
                ancienne.setTelephone(pharmacieModifiee.getTelephone());
            if (pharmacieModifiee.getEmail() != null && !pharmacieModifiee.getEmail().isEmpty())
                ancienne.setEmail(pharmacieModifiee.getEmail());

            try {
                if (registreCommerceFile != null && !registreCommerceFile.isEmpty())
                    ancienne.setRegistreCommerce(registreCommerceFile.getBytes());
                if (cinPharmacienFile != null && !cinPharmacienFile.isEmpty())
                    ancienne.setCinPharmacien(cinPharmacienFile.getBytes());
                if (autorisationMinistereFile != null && !autorisationMinistereFile.isEmpty())
                    ancienne.setAutorisationMinistere(autorisationMinistereFile.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }

            utilisateurRepository.save(ancienne);
        }
        return "redirect:/profil-pharmacie";
    }

    // ===== CHANGEMENT MOT DE PASSE =====
    @Transactional
    @PostMapping("/utilisateur/changer-mot-de-passe")
    public String changerMotDePasseUtilisateur(
            @RequestParam String ancienMotDePasse,
            @RequestParam String nouveauMotDePasse,
            @RequestParam String confirmationMotDePasse,
            HttpSession session
    ) {
        Utilisateur utilisateur = getUtilisateurSession(session);
        if (utilisateur == null) return "redirect:/login";

        if (!utilisateur.getMotDePasse().equals(ancienMotDePasse))
            return "redirect:/profil-utilisateur?error=Ancien mot de passe incorrect";

        if (!nouveauMotDePasse.equals(confirmationMotDePasse))
            return "redirect:/profil-utilisateur?error=Les mots de passe ne correspondent pas";

        utilisateur.setMotDePasse(nouveauMotDePasse);
        utilisateurRepository.save(utilisateur);

        return "redirect:/profil-utilisateur?success=Mot de passe changé avec succès";
    }

    @Transactional
    @PostMapping("/pharmacie/changer-mot-de-passe")
    public String changerMotDePassePharmacie(
            @RequestParam String ancienMotDePasse,
            @RequestParam String nouveauMotDePasse,
            @RequestParam String confirmationMotDePasse,
            HttpSession session
    ) {
        Utilisateur pharmacie = getUtilisateurSession(session);
        if (pharmacie == null) return "redirect:/login";

        if (!pharmacie.getMotDePasse().equals(ancienMotDePasse))
            return "redirect:/profil-pharmacie?error=Ancien mot de passe incorrect";

        if (!nouveauMotDePasse.equals(confirmationMotDePasse))
            return "redirect:/profil-pharmacie?error=Les mots de passe ne correspondent pas";

        pharmacie.setMotDePasse(nouveauMotDePasse);
        utilisateurRepository.save(pharmacie);

        return "redirect:/profil-pharmacie?success=Mot de passe changé avec succès";
    }

    // ===== CONDITIONS =====
    @GetMapping("/conditions")
    public String conditions() {
        return "conditions";
    }

    // ===== ACCUEILS =====
    @GetMapping("/accueil-admin")
    public String accueilAdmin(Model model, HttpSession session) {
        Utilisateur utilisateur = getUtilisateurSession(session);
        if (utilisateur != null) {
            model.addAttribute("utilisateur", utilisateur);
            return "admin/accueil-admin";
        } else return "redirect:/login";
    }

    @GetMapping("/accueil-utilisateur")
    public String accueilUtilisateur(Model model, HttpSession session) {
        Utilisateur utilisateur = getUtilisateurSession(session);
        if (utilisateur != null) {
            model.addAttribute("utilisateur", utilisateur);
            return "utilisateur/accueil-utilisateur";
        } else return "redirect:/login";
    }

    @GetMapping("/accueil-pharmacie")
    public String accueilPharmacie(Model model, HttpSession session) {
        Utilisateur pharmacie = getUtilisateurSession(session);
        if (pharmacie != null) {
            model.addAttribute("utilisateur", pharmacie);

            List<Notification> notifications = new ArrayList<>();
            int unreadCount = 0;
            try {
                notifications = notificationService.getAllNotifications(pharmacie.getId());
                unreadCount = notificationService.getUnreadCount(pharmacie.getId());
            } catch (Exception e) {
                System.out.println("Erreur notifications: " + e.getMessage());
            }

            model.addAttribute("notifications", notifications);
            model.addAttribute("unreadCount", unreadCount);

            return "pharmacie/accueil-pharmacie";
        } else return "redirect:/login";
    }

    // ===== MÉTHODE UTILE =====
    private Utilisateur getUtilisateurSession(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return null;
        return utilisateurRepository.findById(userId).orElse(null);
    }
}
