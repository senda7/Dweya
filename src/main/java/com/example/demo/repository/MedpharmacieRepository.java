package com.example.demo.repository;

import com.example.demo.model.Medpharmacie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedpharmacieRepository extends JpaRepository<Medpharmacie, Long> {

    // Méthodes de recherche générales
    List<Medpharmacie> findByNomContainingIgnoreCase(String nom);
    List<Medpharmacie> findByQuantiteLessThan(int quantite);
    List<Medpharmacie> findByOrdonnanceRequise(boolean ordonnanceRequise);

    // Méthodes de recherche par pharmacie
    List<Medpharmacie> findByPharmacieId(Long pharmacieId);
    List<Medpharmacie> findByPharmacieIdAndNomContainingIgnoreCase(Long pharmacieId, String nom);
    List<Medpharmacie> findByPharmacieIdAndQuantiteLessThan(Long pharmacieId, int quantite);
    List<Medpharmacie> findByPharmacieIdAndOrdonnanceRequise(Long pharmacieId, boolean ordonnanceRequise);

    // Méthodes de comptage générales
    long countByQuantiteLessThan(int quantite);
    long countByOrdonnanceRequise(boolean ordonnanceRequise);

    // Méthodes de comptage par pharmacie
    long countByPharmacieId(Long pharmacieId);
    long countByPharmacieIdAndQuantiteLessThan(Long pharmacieId, int quantite);
    long countByPharmacieIdAndOrdonnanceRequise(Long pharmacieId, boolean ordonnanceRequise);
}