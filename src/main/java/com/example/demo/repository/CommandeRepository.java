package com.example.demo.repository;

import com.example.demo.model.Commande;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CommandeRepository extends JpaRepository<Commande, Long> {

    List<Commande> findByPharmacieIdAndStatut(Long pharmacieId, String statut);

    Optional<Commande> findByIdAndPharmacieId(Long id, Long pharmacieId);

    long countByPharmacieIdAndStatut(Long pharmacieId, String statut);

    List<Commande> findByPharmacieIdAndStatutNot(Long pharmacieId, String statut);

    long countByPharmacieId(Long pharmacieId);

    List<Commande> findByUtilisateurIdAndPharmacieIdAndDateCommande(Long utilisateurId, Long pharmacieId, LocalDateTime dateCommande);

    List<Commande> findByPharmacieId(Long pharmacieId);

    // Charger une commande avec ses médicaments
    @Query("SELECT c FROM Commande c LEFT JOIN FETCH c.medicaments WHERE c.id = :id")
    Optional<Commande> findByIdWithMedicaments(@Param("id") Long id);

    // Charger toutes les commandes d'une pharmacie avec médicaments
    @Query("SELECT DISTINCT c FROM Commande c LEFT JOIN FETCH c.medicaments WHERE c.pharmacieId = :pharmacieId ORDER BY c.dateCommande DESC")
    List<Commande> findByPharmacieIdWithMedicaments(@Param("pharmacieId") Long pharmacieId);

    // Commandes en attente
    @Query("SELECT c FROM Commande c WHERE c.pharmacieId = :pharmacieId AND c.statut = 'EN_ATTENTE' ORDER BY c.dateCommande DESC")
    List<Commande> findCommandesEnAttente(@Param("pharmacieId") Long pharmacieId);

    // Chiffre d'affaires des commandes validées
    @Query("SELECT COALESCE(SUM(c.prixTotal), 0) FROM Commande c WHERE c.pharmacieId = :pharmacieId AND c.statut = 'VALIDEE'")
    double calculerChiffreAffaires(@Param("pharmacieId") Long pharmacieId);

    // Commandes entre deux dates
    @Query("SELECT c FROM Commande c WHERE c.pharmacieId = :pharmacieId AND c.dateCommande BETWEEN :startDate AND :endDate")
    List<Commande> findByPharmacieIdAndDateBetween(@Param("pharmacieId") Long pharmacieId,
                                                   @Param("startDate") LocalDateTime startDate,
                                                   @Param("endDate") LocalDateTime endDate);

    // Commandes récentes depuis une date
    @Query("SELECT c FROM Commande c WHERE c.pharmacieId = :pharmacieId AND c.dateCommande >= :sinceDate ORDER BY c.dateCommande DESC")
    List<Commande> findCommandesRecentes(@Param("pharmacieId") Long pharmacieId,
                                         @Param("sinceDate") LocalDateTime sinceDate);
    // Ajoutez cette méthode dans CommandeRepository
    List<Commande> findByUtilisateurIdAndPharmacieId(Long utilisateurId, Long pharmacieId);

}
