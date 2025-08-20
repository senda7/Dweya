package com.example.demo.controller;

import com.example.demo.model.Don;
import com.example.demo.model.Medicament;
import com.example.demo.model.StatutDon;
import com.example.demo.repository.DonRepository;
import com.example.demo.repository.MedicamentRepository;
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
    private MedicamentRepository medicamentRepository;

    // Liste des dons de l'utilisateur connecté
    @GetMapping("/dons")
    public String listerDons(Model model, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if(userId == null) return "redirect:/login";

        List<Don> mesDons = donRepository.findByMedicament_Utilisateur_Id(userId);
        model.addAttribute("mesDons", mesDons);
        return "utilisateur/mes-dons"; // ton fichier Thymeleaf
    }

    // Formulaire d'ajout
    @GetMapping("/dons/ajouter")
    public String showForm(Model model, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if(userId == null) return "redirect:/login";

        List<Medicament> medicaments = medicamentRepository.findByUtilisateurId(userId);
        model.addAttribute("don", new Don());
        model.addAttribute("medicaments", medicaments);
        return "utilisateur/ajouter-don";
    }

    // Traitement de l'ajout
    @PostMapping("/dons/ajouter")
    public String submitForm(@ModelAttribute Don don,
                             @RequestParam("imageFile") MultipartFile imageFile,
                             HttpSession session) throws IOException {
        Long userId = (Long) session.getAttribute("userId");
        if(userId == null) return "redirect:/login";

        Medicament med = medicamentRepository.findById(don.getMedicament().getId()).orElse(null);
        if(med == null || !med.getUtilisateur().getId().equals(userId)) {
            return "redirect:/dons";
        }

        don.setMedicament(med);
        don.setStatut(StatutDon.EN_COURS);

        if(imageFile != null && !imageFile.isEmpty()) {
            don.setImage(imageFile.getBytes());
        }

        donRepository.save(don);
        return "redirect:/dons";
    }

    // Formulaire de modification
    @GetMapping("/dons/modifier/{id}")
    public String showEditForm(@PathVariable Long id, Model model, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if(userId == null) return "redirect:/login";

        Don don = donRepository.findById(id).orElse(null);
        if(don == null || !don.getMedicament().getUtilisateur().getId().equals(userId)) {
            return "redirect:/dons";
        }

        List<Medicament> medicaments = medicamentRepository.findByUtilisateurId(userId);
        model.addAttribute("don", don);
        model.addAttribute("medicaments", medicaments);
        return "utilisateur/modifier-don";
    }

    // Enregistrer modification
    @PostMapping("/dons/modifier")
    public String editDon(@ModelAttribute Don don,
                          @RequestParam("imageFile") MultipartFile imageFile,
                          HttpSession session) throws IOException {
        Long userId = (Long) session.getAttribute("userId");
        if(userId == null) return "redirect:/login";

        // Récupérer l'objet Don existant
        Don donExistant = donRepository.findById(don.getId()).orElse(null);
        if(donExistant == null || !donExistant.getMedicament().getUtilisateur().getId().equals(userId)) {
            return "redirect:/dons";
        }

        // Mettre à jour le medicament si changé
        Medicament med = medicamentRepository.findById(don.getMedicament().getId()).orElse(null);
        if(med != null && med.getUtilisateur().getId().equals(userId)) {
            donExistant.setMedicament(med);
        }

        // Mettre à jour l'image si un nouveau fichier est fourni
        if(imageFile != null && !imageFile.isEmpty()) {
            donExistant.setImage(imageFile.getBytes());
        }

        // Optionnel: garder le statut existant ou le mettre à jour si nécessaire
        // donExistant.setStatut(don.getStatut());

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
