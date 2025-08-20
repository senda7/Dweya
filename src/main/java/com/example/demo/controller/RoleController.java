package com.example.demo.controller;

import com.example.demo.model.Role;
import com.example.demo.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/roles")
public class RoleController {

    @Autowired
    private RoleRepository roleRepository;

    // Afficher tous les rôles
    @GetMapping
    public String afficherRoles(Model model) {
        model.addAttribute("roles", roleRepository.findAll());
        model.addAttribute("nouveauRole", new Role());
        return "admin/roles";  // chercher roles.html داخل مجلد admin
    }

    // Ajouter un nouveau rôle
    @PostMapping("/ajouter")
    public String ajouterRole(@ModelAttribute("nouveauRole") Role role) {
        if (role.getNom() != null && !role.getNom().isEmpty()) {
            roleRepository.save(role);
        }
        return "redirect:/admin/roles";
    }

    // Supprimer un rôle
    @GetMapping("/supprimer/{id}")
    public String supprimerRole(@PathVariable Long id) {
        roleRepository.deleteById(id);
        return "redirect:/admin/roles";
    }
}
