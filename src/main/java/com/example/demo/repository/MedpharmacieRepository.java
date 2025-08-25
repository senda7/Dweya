package com.example.demo.repository;

import com.example.demo.model.Medpharmacie;
import com.example.demo.model.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MedpharmacieRepository extends JpaRepository<Medpharmacie, Long> {

    // 🔹 Tous les médicaments d'une pharmacie
    List<Medpharmacie> findByPharmacie(Utilisateur pharmacie);

    // 🔹 Rechercher par nom dans une pharmacie
    List<Medpharmacie> findByPharmacieAndNomContainingIgnoreCase(Utilisateur pharmacie, String nom);

    // 🔹 Récupérer un médicament par ID et pharmacie
    Optional<Medpharmacie> findByIdAndPharmacie(Long id, Utilisateur pharmacie);

    // 🔹 Statistiques
    long countByPharmacie(Utilisateur pharmacie);
    long countByPharmacieAndQuantiteLessThan(Utilisateur pharmacie, int quantite);
    long countByPharmacieAndOrdonnanceRequise(Utilisateur pharmacie, boolean ordonnanceRequise);
}
