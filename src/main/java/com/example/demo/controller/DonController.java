package com.example.demo.controller;

import com.example.demo.model.Don;
import com.example.demo.model.Medicament;
import com.example.demo.model.StatutDon;
import com.example.demo.model.Utilisateur;
import com.example.demo.repository.DonRepository;
import com.example.demo.repository.MedicamentRepository;
import com.example.demo.repository.UtilisateurRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Controller
public class DonController {

    @Autowired
    private DonRepository donRepository;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private MedicamentRepository medicamentRepository;

    // Page Espace des Dons - CORRIGÉ
    @GetMapping("/espace-dons")
    public String espaceDons(Model model, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";

        // Récupérer l'utilisateur connecté
        Utilisateur utilisateur = utilisateurRepository.findById(userId).orElse(null);
        if (utilisateur != null) {
            model.addAttribute("utilisateur", utilisateur);
        }

        return "utilisateur/espace-dons";
    }

    // Liste des dons de l'utilisateur connecté
    @GetMapping("/dons")
    public String listerDons(Model model, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if(userId == null) return "redirect:/login";

        // Récupérer l'utilisateur connecté
        Utilisateur utilisateur = utilisateurRepository.findById(userId).orElse(null);
        if (utilisateur != null) {
            model.addAttribute("utilisateur", utilisateur);
        }

        List<Don> mesDons = donRepository.findByMedicament_Utilisateur_IdAndStatut(userId, StatutDon.EN_COURS);
        model.addAttribute("mesDons", mesDons);
        return "utilisateur/mes-dons";
    }

    // Formulaire d'ajout
    @GetMapping("/dons/ajouter")
    public String showForm(Model model, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if(userId == null) return "redirect:/login";

        // Récupérer l'utilisateur connecté
        Utilisateur utilisateur = utilisateurRepository.findById(userId).orElse(null);
        if (utilisateur != null) {
            model.addAttribute("utilisateur", utilisateur);
        }

        List<Medicament> medicaments = medicamentRepository.findByUtilisateurId(userId);
        model.addAttribute("don", new Don());
        model.addAttribute("medicaments", medicaments);

        List<Utilisateur> pharmacies = utilisateurRepository.findByRoleId(3L);
        model.addAttribute("pharmacies", pharmacies);

        return "utilisateur/ajouter-don";
    }

    // Traitement de l'ajout
    @PostMapping("/dons/ajouter")
    public String submitForm(@ModelAttribute Don don,
                             @RequestParam("imageFile") MultipartFile imageFile,
                             HttpSession session) throws IOException {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";

        // Vérification des médicaments
        Medicament med = medicamentRepository.findById(don.getMedicament().getId()).orElse(null);
        if (med == null || !med.getUtilisateur().getId().equals(userId)) {
            return "redirect:/dons";
        }
        don.setMedicament(med);
        //la pharmacie que vous avez choisie dans le formulaire
        Utilisateur pharmacie = utilisateurRepository.findById(don.getPharmacie().getId()).orElse(null);
        if (pharmacie == null || pharmacie.getRole().getId() != 3) {
            return "redirect:/dons";
        }
        don.setPharmacie(pharmacie);
        don.setStatut(StatutDon.EN_COURS);

        if (imageFile != null && !imageFile.isEmpty()) {
            don.setImage(imageFile.getBytes());
        }
        donRepository.save(don);

        return "redirect:/dons";
    }

    // Formulaire de modification
    @GetMapping("/dons/modifier/{id}")
    public String showEditForm(@PathVariable Long id, Model model, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";

        // Récupérer l'utilisateur connecté
        Utilisateur utilisateur = utilisateurRepository.findById(userId).orElse(null);
        if (utilisateur != null) {
            model.addAttribute("utilisateur", utilisateur);
        }

        Don don = donRepository.findById(id).orElse(null);
        if (don == null || !don.getMedicament().getUtilisateur().getId().equals(userId)) {
            return "redirect:/dons";
        }

        // récupérer les médicaments de l'utilisateur connecté
        List<Medicament> medicaments = medicamentRepository.findByUtilisateurId(userId);
        // récupérer les pharmacies (les utilisateurs qui ont le rôle PHARMACIE)
        List<Utilisateur> pharmacies = utilisateurRepository.findByRoleId(3L);
        model.addAttribute("pharmacies", pharmacies);

        model.addAttribute("don", don);
        model.addAttribute("medicaments", medicaments);
        model.addAttribute("pharmacies", pharmacies);

        return "utilisateur/modifier-don";
    }

    // Enregistrer modification
    @PostMapping("/dons/modifier")
    public String editDon(@ModelAttribute Don don,
                          @RequestParam("imageFile") MultipartFile imageFile,
                          HttpSession session) throws IOException {

        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";

        Don donExistant = donRepository.findById(don.getId()).orElse(null);
        if (donExistant == null || !donExistant.getMedicament().getUtilisateur().getId().equals(userId)) {
            return "redirect:/dons";
        }

        // update médicament
        Medicament med = medicamentRepository.findById(don.getMedicament().getId()).orElse(null);
        if (med != null && med.getUtilisateur().getId().equals(userId)) {
            donExistant.setMedicament(med);
        }

        // update pharmacie directement depuis l'objet
        donExistant.setPharmacie(don.getPharmacie());
        // update description
        donExistant.setDescription(don.getDescription());
        // update image
        if (imageFile != null && !imageFile.isEmpty()) {
            donExistant.setImage(imageFile.getBytes());
        }

        donRepository.save(donExistant);
        return "redirect:/dons";
    }

    // Supprimer un don
    @GetMapping("/dons/supprimer/{id}")
    public String supprimer(@PathVariable Long id, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if(userId == null) return "redirect:/login";

        Don don = donRepository.findById(id).orElse(null);
        if(don != null && don.getMedicament().getUtilisateur().getId().equals(userId)) {
            donRepository.deleteById(id);
        }

        return "redirect:/dons";
    }

    // Affichage image
    @GetMapping("/dons/image/{id}")
    @ResponseBody
    public byte[] getImage(@PathVariable Long id) {
        Don don = donRepository.findById(id).orElse(null);
        if(don != null && don.getImage() != null) {
            return don.getImage();
        }
        return new byte[0];
    }
}