package com.example.demo.controller;

import com.example.demo.model.Utilisateur;
import com.example.demo.repository.UtilisateurRepository;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@Controller
public class UtilisateurController {

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    //----------- Liste des utilisateurs simples
    @GetMapping("/admin/utilisateurs")
    public String listeUtilisateurs(Model model) {
        List<Utilisateur> utilisateurs = utilisateurRepository.findAll()
                .stream()
                .filter(u -> u.getTypeUtilisateur() == Utilisateur.TypeUtilisateur.UTILISATEUR)
                .toList();
        model.addAttribute("utilisateurs", utilisateurs);
        return "admin/liste-utilisateur";
    }

    //---------- Liste des pharmacies uniquement
    @GetMapping("/admin/liste-pharmacies")
    public String listePharmacies(Model model) {
        List<Utilisateur> pharmacies = utilisateurRepository.findAll()
                .stream()
                .filter(u -> u.getTypeUtilisateur() == Utilisateur.TypeUtilisateur.PHARMACIE)
                .toList();
        model.addAttribute("pharmacies", pharmacies);
        return "admin/liste-pharmacies";
    }

    //----------- Désactiver un compte
    @GetMapping("/utilisateur/desactiver/{id}")
    public String desactiver(@PathVariable Long id) {
        utilisateurRepository.findById(id).ifPresent(u -> {
            u.setEtat(false);
            utilisateurRepository.save(u);
        });
        return "redirect:/admin/utilisateurs";
    }

    @GetMapping("/pharmacie/desactiver/{id}")
    public String desactiverPharmacie(@PathVariable Long id) {
        utilisateurRepository.findById(id).ifPresent(u -> {
            u.setEtat(false);
            utilisateurRepository.save(u);
        });
        return "redirect:/admin/liste-pharmacies";
    }

    //---------Activer un compte
    @GetMapping("/utilisateur/activer/{id}")
    public String activer(@PathVariable Long id) {
        utilisateurRepository.findById(id).ifPresent(u -> {
            u.setEtat(true);
            utilisateurRepository.save(u);
        });
        return "redirect:/admin/utilisateurs";
    }

    @GetMapping("/pharmacie/activer/{id}")
    public String activerPharmacie(@PathVariable Long id) {
        utilisateurRepository.findById(id).ifPresent(u -> {
            u.setEtat(true);
            utilisateurRepository.save(u);
        });
        return "redirect:/admin/liste-pharmacies";
    }

    //---------Supprimer un compte
    @GetMapping("/utilisateur/supprimer/{id}")
    public String supprimer(@PathVariable Long id) {
        utilisateurRepository.deleteById(id);
        return "redirect:/admin/utilisateurs";
    }

    @GetMapping("/pharmacie/supprimer/{id}")
    public String supprimerPharmacie(@PathVariable Long id) {
        utilisateurRepository.deleteById(id);
        return "redirect:/admin/liste-pharmacies";
    }

    //----------Afficher un fichier binaire : registre, CIN, autorisation
    @GetMapping("/fichier/voir/{type}/{id}")
    public void voirFichier(@PathVariable String type, @PathVariable Long id, HttpServletResponse response) throws IOException {
        utilisateurRepository.findById(id).ifPresent(u -> {
            try {
                byte[] data = switch (type) {
                    case "registre" -> u.getRegistreCommerce();
                    case "cin" -> u.getCinPharmacien();
                    case "autorisation" -> u.getAutorisationMinistere();
                    default -> null;
                };

                if (data != null) {
                    response.setContentType("application/pdf");
                    response.getOutputStream().write(data);
                    response.getOutputStream().flush();
                } else {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
