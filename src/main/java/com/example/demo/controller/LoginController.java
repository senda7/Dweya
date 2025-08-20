package com.example.demo.controller;

import com.example.demo.model.Role;
import com.example.demo.model.Utilisateur;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UtilisateurRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.annotation.Transactional;


import java.io.IOException;

@Controller
public class LoginController {

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private RoleRepository roleRepository;

    // --- Affichage page login ---
    @GetMapping("/login")
    public String loginForm(Model model) {
        model.addAttribute("utilisateur", new Utilisateur());
        return "login";
    }

    // --- Traitement login ---
    @PostMapping("/login")
    public String loginSubmit(@ModelAttribute Utilisateur utilisateur, Model model, HttpSession session) {
        Utilisateur u = utilisateurRepository.findByEmailAndMotDePasse(utilisateur.getEmail(), utilisateur.getMotDePasse());

        if (u != null && u.isEtat()) {
            session.setAttribute("userId", u.getId());
            session.setAttribute("role", u.getRole().getNom());

            switch (u.getTypeUtilisateur()) {
                case ADMIN:
                    return "redirect:/accueil-admin";
                case PHARMACIE:
                    return "redirect:/accueil-pharmacie";
                case UTILISATEUR:
                default:
                    return "redirect:/accueil-utilisateur";
            }
        } else {
            model.addAttribute("error", "Email ou mot de passe incorrect, ou compte inactif");
            return "login";
        }
    }

    // --- Page register ---
    @GetMapping("/register")
    public String registerForm(Model model) {
        var roles = roleRepository.findAll();
        model.addAttribute("utilisateur", new Utilisateur());
        model.addAttribute("roles", roles);
        return "register";
    }

    // --- Traitement inscription ---
    // --- Traitement inscription ---
    @PostMapping("/register")
    public String registerSubmit(
            @ModelAttribute Utilisateur utilisateur,
            @RequestParam(value = "roleId", required = false) Long roleId,
            Model model
    ) {
        // Vérification du rôle
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
                utilisateur.setEtat(false); // compte en attente de validation

                // Conversion des fichiers MultipartFile en byte[]
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
                // Utilisateur classique ou autre rôle
                utilisateur.setTypeUtilisateur(Utilisateur.TypeUtilisateur.UTILISATEUR);
                utilisateur.setEtat(true); // compte actif directement
            }
        } catch (IOException e) {
            e.printStackTrace();
            model.addAttribute("erreur", "Erreur lors du traitement des fichiers.");
            model.addAttribute("roles", roleRepository.findAll());
            return "register";
        }

        // Sauvegarde de l'utilisateur
        utilisateurRepository.save(utilisateur);

        // Redirection ou message selon le rôle
        if ("pharmacie".equals(roleName)) {
            model.addAttribute("message", "Votre compte a été créé avec succès. En attente de validation par l’administrateur.");
            return "login";
        } else {
            return "redirect:/login";
        }
    }



    // --- --------Affichage profil utilisateur ---
    @GetMapping("/profil-utilisateur")
    public String profilUtilisateur(Model model, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";

        Utilisateur utilisateur = utilisateurRepository.findById(userId).orElse(null);
        if (utilisateur == null) return "redirect:/login";

        model.addAttribute("utilisateur", utilisateur);

        return "utilisateur/profil-utilisateur";
    }

    // --- ---------Affichage profil utilisateur pharma ---
    @GetMapping("/profil-pharmacie")
    public String profilPharmacie(Model model, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";

        Utilisateur utilisateur = utilisateurRepository.findById(userId).orElse(null);
        if (utilisateur == null) return "redirect:/login";

        model.addAttribute("utilisateur", utilisateur);

        return "pharmacie/profil-pharmacie"; // vérifie aussi le nom du fichier HTML dans templates
    }

    // --- Endpoint pour afficher la photo de profil ---
    @GetMapping("/utilisateur/photo/{id}")
    @ResponseBody
    public ResponseEntity<byte[]> getPhoto(@PathVariable Long id) {
        Utilisateur user = utilisateurRepository.findById(id).orElse(null);

        if (user == null || user.getPhotoProfil() == null) {
            return ResponseEntity.notFound().build();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG); // ⚡ Change en JPEG si nécessaire

        return ResponseEntity.ok().headers(headers).body(user.getPhotoProfil());
    }

    // --- Modification du profil utilisateur ---
    @PostMapping("/utilisateur/modifier")
    public String modifierUtilisateur(@ModelAttribute Utilisateur utilisateurModifie) {
        Utilisateur ancien = utilisateurRepository.findById(utilisateurModifie.getId()).orElse(null);

        if (ancien != null) {
            if (utilisateurModifie.getNom() != null && !utilisateurModifie.getNom().isEmpty()) {
                ancien.setNom(utilisateurModifie.getNom());
            }
            if (utilisateurModifie.getPrenom() != null && !utilisateurModifie.getPrenom().isEmpty()) {
                ancien.setPrenom(utilisateurModifie.getPrenom());
            }
            if (utilisateurModifie.getGenre() != null && !utilisateurModifie.getGenre().isEmpty()) {
                ancien.setGenre(utilisateurModifie.getGenre());
            }
            if (utilisateurModifie.getDateNaissance() != null) {
                ancien.setDateNaissance(utilisateurModifie.getDateNaissance());
            }
            if (utilisateurModifie.getEmail() != null && !utilisateurModifie.getEmail().isEmpty()) {
                ancien.setEmail(utilisateurModifie.getEmail());
            }
            if (utilisateurModifie.getTelephone() != null && !utilisateurModifie.getTelephone().isEmpty()) {
                ancien.setTelephone(utilisateurModifie.getTelephone());
            }
            if (utilisateurModifie.getAdresse() != null && !utilisateurModifie.getAdresse().isEmpty()) {
                ancien.setAdresse(utilisateurModifie.getAdresse());
            }
            if (utilisateurModifie.getVille() != null && !utilisateurModifie.getVille().isEmpty()) {
                ancien.setVille(utilisateurModifie.getVille());
            }

            utilisateurRepository.save(ancien);
        }

        return "redirect:/profil-utilisateur";
    }
    //______modification de donnees pharma--------
    @PostMapping("/pharmacie/modifier")
    public String modifierPharmacie(
            @ModelAttribute Utilisateur pharmacieModifiee,
            @RequestParam(value = "registreCommerce", required = false) MultipartFile registreCommerceFile,
            @RequestParam(value = "cinPharmacien", required = false) MultipartFile cinPharmacienFile,
            @RequestParam(value = "autorisationMinistere", required = false) MultipartFile autorisationMinistereFile
    ) {
        Utilisateur ancienne = utilisateurRepository.findById(pharmacieModifiee.getId()).orElse(null);

        if (ancienne != null) {
            if (pharmacieModifiee.getNomPharmacie() != null && !pharmacieModifiee.getNomPharmacie().isEmpty()) {
                ancienne.setNomPharmacie(pharmacieModifiee.getNomPharmacie());
            }
            if (pharmacieModifiee.getNom() != null && !pharmacieModifiee.getNom().isEmpty()) {
                ancienne.setNom(pharmacieModifiee.getNom());
            }
            if (pharmacieModifiee.getPrenom() != null && !pharmacieModifiee.getPrenom().isEmpty()) {
                ancienne.setPrenom(pharmacieModifiee.getPrenom());
            }
            if (pharmacieModifiee.getNumeroLicence() != null && !pharmacieModifiee.getNumeroLicence().isEmpty()) {
                ancienne.setNumeroLicence(pharmacieModifiee.getNumeroLicence());
            }
            if (pharmacieModifiee.getNumeroOrdre() != null && !pharmacieModifiee.getNumeroOrdre().isEmpty()) {
                ancienne.setNumeroOrdre(pharmacieModifiee.getNumeroOrdre());
            }
            if (pharmacieModifiee.getAdresse() != null && !pharmacieModifiee.getAdresse().isEmpty()) {
                ancienne.setAdresse(pharmacieModifiee.getAdresse());
            }
            if (pharmacieModifiee.getTelephone() != null && !pharmacieModifiee.getTelephone().isEmpty()) {
                ancienne.setTelephone(pharmacieModifiee.getTelephone());
            }
            if (pharmacieModifiee.getEmail() != null && !pharmacieModifiee.getEmail().isEmpty()) {
                ancienne.setEmail(pharmacieModifiee.getEmail());
            }

            try {
                if (registreCommerceFile != null && !registreCommerceFile.isEmpty()) {
                    ancienne.setRegistreCommerce(registreCommerceFile.getBytes());
                }
                if (cinPharmacienFile != null && !cinPharmacienFile.isEmpty()) {
                    ancienne.setCinPharmacien(cinPharmacienFile.getBytes());
                }
                if (autorisationMinistereFile != null && !autorisationMinistereFile.isEmpty()) {
                    ancienne.setAutorisationMinistere(autorisationMinistereFile.getBytes());
                }
            } catch (IOException e) {
                e.printStackTrace(); // ou logger.error(...)
            }

            utilisateurRepository.save(ancienne);
        }

        return "redirect:/profil-pharmacie";
    }


    // --- Changer mot de passe  de utilisateur---
    @Transactional
    @PostMapping("/utilisateur/changer-mot-de-passe")
    public String changerMotDePasse(
            @RequestParam String ancienMotDePasse,
            @RequestParam String nouveauMotDePasse,
            @RequestParam String confirmationMotDePasse,
            HttpSession session
    ) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";

        Utilisateur utilisateur = utilisateurRepository.findById(userId).orElse(null);
        if (utilisateur == null) return "redirect:/login";

        // Vérifier ancien mot de passe
        if (!utilisateur.getMotDePasse().equals(ancienMotDePasse)) {
            return "redirect:/profil-utilisateur?error=Ancien mot de passe incorrect";
        }

        // Vérifier confirmation
        if (!nouveauMotDePasse.equals(confirmationMotDePasse)) {
            return "redirect:/profil-utilisateur?error=Les mots de passe ne correspondent pas";
        }

        utilisateur.setMotDePasse(nouveauMotDePasse);
        utilisateurRepository.save(utilisateur);

        return "redirect:/profil-utilisateur?success=Mot de passe changé avec succès";
    }


    // --- Changer photo de profil utilisateur ---
    @PostMapping("/utilisateur/changer-photo")
    public String changerPhoto(@RequestParam("photoProfil") MultipartFile photo, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";

        Utilisateur utilisateur = utilisateurRepository.findById(userId).orElse(null);
        if (utilisateur == null) return "redirect:/login";

        if (photo != null && !photo.isEmpty()) {
            try {
                utilisateur.setPhotoProfil(photo.getBytes());
                utilisateurRepository.save(utilisateur);
            } catch (IOException e) {
                e.printStackTrace();
                return "redirect:/profil-utilisateur?error";
            }
        }

        return "redirect:/profil-utilisateur?success";
    }
    //----------changer photo de profil pharamacie-----
    @PostMapping("/pharmacie/changer-photo")
    public String changerPhotos(@RequestParam("photoProfil") MultipartFile photo, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";

        Utilisateur utilisateur = utilisateurRepository.findById(userId).orElse(null);
        if (utilisateur == null) return "redirect:/login";

        if (photo != null && !photo.isEmpty()) {
            try {
                utilisateur.setPhotoProfil(photo.getBytes());
                utilisateurRepository.save(utilisateur);
            } catch (IOException e) {
                e.printStackTrace();
                return "redirect:/profil-pharmacie?error";
            }
        }
        return "redirect:/profil-pharmacie?success";
    }

    // conditions.html dans templates
    @GetMapping("/conditions")
    public String conditions() {
        return "conditions";
    }
    // --- Déconnexion ---
    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
    // --- Accueils personnalisés ---
    //-----------------accueil admin---------
    @GetMapping("/accueil-admin")
    public String accueilAdmin(Model model, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId != null) {
            Utilisateur utilisateur = utilisateurRepository.findById(userId).orElse(null);
            model.addAttribute("utilisateur", utilisateur);
            return "admin/accueil-admin";
        } else {
            return "redirect:/login";
        }
    }
    //-----------------accueil user---------
    @GetMapping("/accueil-utilisateur")
    public String accueilUtilisateur(Model model, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId != null) {
            Utilisateur utilisateur = utilisateurRepository.findById(userId).orElse(null);
            model.addAttribute("utilisateur", utilisateur);
            return "utilisateur/accueil-utilisateur";
        } else {
            return "redirect:/login";
        }
    }
    //-----------------accueil pharma---------
    @GetMapping("/accueil-pharmacie")
    public String accueilPharmacie(Model model, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId != null) {
            Utilisateur utilisateur = utilisateurRepository.findById(userId).orElse(null);
            model.addAttribute("utilisateur", utilisateur);
            return "pharmacie/accueil-pharmacie";
        } else {
            return "redirect:/login";
        }
    }

  //************************************





}
