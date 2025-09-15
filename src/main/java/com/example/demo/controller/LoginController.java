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
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import java.util.Optional;

@Controller
public class LoginController {
    @Autowired
    private NotificationService notificationService;

    @Autowired
    private AuthService authService;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private RoleRepository roleRepository;

    // ======= PAGE LOGIN =======
    @GetMapping("/login")
    public String loginPage(Model model) {
        model.addAttribute("utilisateur", new Utilisateur());
        return "login";
    }

    // ======= TRAITEMENT LOGIN =======
    @PostMapping("/login")
    public String loginSubmit(@RequestParam(required = false) String email,
                              @RequestParam(required = false) String motDePasse,
                              @ModelAttribute Utilisateur utilisateur,
                              Model model,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {

        Utilisateur u = null;

        if (email != null && motDePasse != null) {
            // Login via AuthService (méthode recommandée)
            u = authService.login(email, motDePasse);
        } else if (utilisateur.getEmail() != null && utilisateur.getMotDePasse() != null) {
            // Login via repository - CORRIGÉ : Utiliser la méthode utilitaire
            Optional<Utilisateur> utilisateurOpt = utilisateurRepository.findFirstByEmailAndMotDePasse(
                    utilisateur.getEmail(),
                    utilisateur.getMotDePasse()
            );
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

    // ======= LOGOUT =======
    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    // ======= REGISTER =======
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

        // Vérifier si l'email existe déjà
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

    // ======= PROFILS =======
    @GetMapping("/profil-utilisateur")
    public String profilUtilisateur(Model model, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";
        model.addAttribute("utilisateur", utilisateurRepository.findById(userId).orElse(null));
        return "utilisateur/profil-utilisateur";
    }

    @GetMapping("/profil-admin")
    public String profilAdmin(Model model, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";
        model.addAttribute("utilisateur", utilisateurRepository.findById(userId).orElse(null));
        return "admin/profil-admin";
    }

    @GetMapping("/profil-pharmacie")
    public String profilPharmacie(Model model, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";
        model.addAttribute("utilisateur", utilisateurRepository.findById(userId).orElse(null));
        return "pharmacie/profil-pharmacie";
    }

    // ======= ACCUEILS =======
    @GetMapping("/accueil-admin")
    public String accueilAdmin(Model model, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId != null) model.addAttribute("utilisateur", utilisateurRepository.findById(userId).orElse(null));
        else return "redirect:/login";
        return "admin/accueil-admin";
    }

    @GetMapping("/accueil-utilisateur")
    public String accueilUtilisateur(Model model, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId != null) model.addAttribute("utilisateur", utilisateurRepository.findById(userId).orElse(null));
        else return "redirect:/login";
        return "utilisateur/accueil-utilisateur";
    }

    @GetMapping("/accueil-pharmacie")
    public String accueilPharmacie(Model model, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        Utilisateur utilisateur = utilisateurRepository.findById(userId).orElse(null);
        model.addAttribute("utilisateur", utilisateur);

        // Initialiser avec des valeurs par défaut
        List<Notification> notifications = new ArrayList<>();
        int unreadCount = 0;

        try {
            notifications = notificationService.getAllNotifications(userId);
            unreadCount = notificationService.getUnreadCount(userId);
        } catch (Exception e) {
            // Log l'erreur mais continue avec les valeurs par défaut
            System.out.println("Erreur lors du chargement des notifications: " + e.getMessage());
        }

        model.addAttribute("notifications", notifications);
        model.addAttribute("unreadCount", unreadCount);

        return "pharmacie/accueil-pharmacie";
    }
}