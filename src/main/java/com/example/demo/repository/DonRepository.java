package com.example.demo.repository;

import com.example.demo.model.Don;
import com.example.demo.model.Medicament;
import com.example.demo.model.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.demo.model.StatutDon;

import java.util.List;

@Repository
public interface DonRepository extends JpaRepository<Don, Long> {

    // Récupérer tous les dons liés aux médicaments d'un utilisateurs
    List<Don> findByMedicament_Utilisateur_Id(Long utilisateurId);

    // Récupérer tous les dons pour un médicament spécifique
    List<Don> findByMedicament(Medicament medicament);

    // Optionnel : récupérer tous les dons d'un utilisateur (via médicament)
    List<Don> findByMedicament_Utilisateur(Utilisateur utilisateur);
    List<Don> findByStatut(StatutDon statut);

    // Méthode pour récupérer tous les dons avec un statut dans une liste
    List<Don> findByStatutIn(List<StatutDon> statuts);
}
