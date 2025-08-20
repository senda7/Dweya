package com.example.demo.repository;

import com.example.demo.model.Utilisateur;
import com.example.demo.model.Utilisateur.TypeUtilisateur;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {

    // Pour login
    Utilisateur findByEmailAndMotDePasse(String email, String motDePasse);

    // 🔹 Pour gérer les utilisateurs selon leur type (admin, utilisateur, pharmacie)
    List<Utilisateur> findByTypeUtilisateur(TypeUtilisateur typeUtilisateur);

    // 🔹 Pour lister les pharmacies en attente de validation (etat = false)
    List<Utilisateur> findByTypeUtilisateurAndEtat(TypeUtilisateur typeUtilisateur, boolean etat);

    // 🔹 Pour la récupération de compte par email
    Utilisateur findByEmail(String email);
}

