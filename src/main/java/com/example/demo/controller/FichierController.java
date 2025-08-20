package com.example.demo.controller;

import com.example.demo.model.Utilisateur;
import com.example.demo.repository.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
@RequestMapping("/fichiers")
public class FichierController {

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    // 📂 Détecte le type MIME à partir des premiers octets du fichier
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

    // 📂 Méthode générique pour servir le fichier
    private ResponseEntity<ByteArrayResource> getFile(byte[] data, String filename) {
        if (data == null || data.length == 0) return ResponseEntity.notFound().build();

        String contentType = detectMimeType(data);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType(contentType))
                .contentLength(data.length)
                .body(new ByteArrayResource(data));
    }

    // 📂 Registre de commerce
    @GetMapping("/registreCommerce/{id}")
    public ResponseEntity<ByteArrayResource> voirRegistre(@PathVariable Long id) {
        Optional<Utilisateur> utilisateurOpt = utilisateurRepository.findById(id);
        return utilisateurOpt.map(u -> getFile(u.getRegistreCommerce(), "registre_commerce"))
                .orElse(ResponseEntity.notFound().build());
    }

    // 📂 CIN pharmacien
    @GetMapping("/cinPharmacien/{id}")
    public ResponseEntity<ByteArrayResource> voirCIN(@PathVariable Long id) {
        Optional<Utilisateur> utilisateurOpt = utilisateurRepository.findById(id);
        return utilisateurOpt.map(u -> getFile(u.getCinPharmacien(), "cin_pharmacien"))
                .orElse(ResponseEntity.notFound().build());
    }

    // 📂 Autorisation Ministère
    @GetMapping("/autorisationMinistere/{id}")
    public ResponseEntity<ByteArrayResource> voirAutorisation(@PathVariable Long id) {
        Optional<Utilisateur> utilisateurOpt = utilisateurRepository.findById(id);
        return utilisateurOpt.map(u -> getFile(u.getAutorisationMinistere(), "autorisation"))
                .orElse(ResponseEntity.notFound().build());
    }
}
