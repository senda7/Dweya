package com.example.demo.controller;

import com.example.demo.model.Utilisateur;
import com.example.demo.repository.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class PharmacieController {

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @GetMapping("/pharmacies")
    public String showPharmaciesPage(Model model) {
        // Utilisation de votre méthode existante pour récupérer les pharmacies.html actives
        List<Utilisateur> pharmacies = utilisateurRepository.findByTypeUtilisateurAndEtat(
                Utilisateur.TypeUtilisateur.PHARMACIE,
                true
        );

        // Extraire la liste des villes uniques pour le filtre
        List<String> villes = pharmacies.stream()
                .map(Utilisateur::getVille)
                .distinct()
                .collect(Collectors.toList());

        model.addAttribute("pharmacies", pharmacies);
        model.addAttribute("villes", villes);
        return "pharmacies";
    }
}