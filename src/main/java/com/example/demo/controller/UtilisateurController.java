
package com.example.demo.controller;

import com.example.demo.model.Utilisateur;
import com.example.demo.repository.UtilisateurRepository;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.Optional;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.core.io.ByteArrayResource;

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

    //---------- Liste des pharmacies.html uniquement
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
    //  Détecte le type MIME à partir des premiers octets du fichier
    private String detectMimeType(byte[] data) {
        if (data == null || data.length < 4) return "application/octet-stream";
        // PDF : "%PDF"
        if (data[0] == 0x25 && data[1] == 0x50 && data[2] == 0x44 && data[3] == 0x46) return "application/pdf";
        // JPEG : 0xFF 0xD8
        if (data[0] == (byte)0xFF && data[1] == (byte)0xD8) return "image/jpeg";
        // PNG : 0x89 0x50 0x4E 0x47
        if (data[0] == (byte)0x89 && data[1] == 0x50 && data[2] == 0x4E && data[3] == 0x47) return "image/png";
        // GIF : "GIF8"
        if (data[0] == 0x47 && data[1] == 0x49 && data[2] == 0x46 && data[3] == 0x38) return "image/gif";

        return "application/octet-stream";
    }

    // Méthode générique pour servir le fichier
    private ResponseEntity<ByteArrayResource> getFile(byte[] data, String filename) {
        if (data == null || data.length == 0) return ResponseEntity.notFound().build();

        String contentType = detectMimeType(data);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType(contentType))
                .contentLength(data.length)
                .body(new ByteArrayResource(data));
    }

    //  Registre de commerce
    @GetMapping("/fichier/voir/registre/{id}")
    public ResponseEntity<ByteArrayResource> voirRegistre(@PathVariable Long id) {
        Optional<Utilisateur> utilisateurOpt = utilisateurRepository.findById(id);
        return utilisateurOpt.map(u -> getFile(u.getRegistreCommerce(), "registre_commerce.pdf"))
                .orElse(ResponseEntity.notFound().build());
    }

    // CIN pharmacien
    @GetMapping("/fichier/voir/cin/{id}")
    public ResponseEntity<ByteArrayResource> voirCIN(@PathVariable Long id) {
        Optional<Utilisateur> utilisateurOpt = utilisateurRepository.findById(id);
        return utilisateurOpt.map(u -> getFile(u.getCinPharmacien(), "cin_pharmacien.pdf"))
                .orElse(ResponseEntity.notFound().build());
    }

    //  Autorisation Ministère
    @GetMapping("/fichier/voir/autorisation/{id}")
    public ResponseEntity<ByteArrayResource> voirAutorisation(@PathVariable Long id) {
        Optional<Utilisateur> utilisateurOpt = utilisateurRepository.findById(id);
        return utilisateurOpt.map(u -> getFile(u.getAutorisationMinistere(), "autorisation.pdf"))
                .orElse(ResponseEntity.notFound().build());
    }

}
