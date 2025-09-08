package com.example.demo.repository;

import com.example.demo.model.Medpharmacie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface MedpharmacieRepository extends JpaRepository<Medpharmacie, Long> {

    // ===== MÉTHODES DE BASE =====
    List<Medpharmacie> findAll();
    Optional<Medpharmacie> findById(Long id);
    boolean existsById(Long id);

    // ===== MÉTHODES SPÉCIFIQUES PAR PHARMACIE =====
    List<Medpharmacie> findByPharmacieId(Long pharmacieId);
    Optional<Medpharmacie> findByIdAndPharmacieId(Long id, Long pharmacieId);
    boolean existsByIdAndPharmacieId(Long id, Long pharmacieId);

    // Recherche par nom et pharmacie
    List<Medpharmacie> findByPharmacieIdAndNomContainingIgnoreCase(Long pharmacieId, String nom);

    // Recherche par quantité et pharmacie
    List<Medpharmacie> findByPharmacieIdAndQuantite(Long pharmacieId, int quantite);
    List<Medpharmacie> findByPharmacieIdAndQuantiteGreaterThan(Long pharmacieId, int quantite);
    List<Medpharmacie> findByPharmacieIdAndQuantiteLessThan(Long pharmacieId, int quantite);

    // ===== MÉTHODES DE RECHERCHE GÉNÉRALES =====
    List<Medpharmacie> findByNomContainingIgnoreCase(String nom);
    List<Medpharmacie> findByQuantiteLessThan(int quantite);
    List<Medpharmacie> findByOrdonnanceRequise(boolean ordonnanceRequise);

    // Méthode pour trouver par nom exact
    Optional<Medpharmacie> findByNomIgnoreCase(String nom);
    boolean existsByNomIgnoreCase(String nom);

    // ===== MÉTHODES DE COMPTAGE =====
    long count();
    long countByPharmacieId(Long pharmacieId);
    long countByQuantiteLessThan(int quantite);
    long countByPharmacieIdAndQuantiteLessThan(Long pharmacieId, int quantite);

    // ===== MÉTHODES POUR LA MISE À JOUR DU STOCK =====
    @Transactional
    @Modifying
    @Query("UPDATE Medpharmacie m SET m.quantite = m.quantite - :quantite WHERE m.id = :id AND m.pharmacieId = :pharmacieId AND m.quantite >= :quantite")
    int decrementerStock(@Param("id") Long id, @Param("pharmacieId") Long pharmacieId, @Param("quantite") int quantite);

    @Transactional
    @Modifying
    @Query("UPDATE Medpharmacie m SET m.quantite = m.quantite + :quantite WHERE m.id = :id AND m.pharmacieId = :pharmacieId")
    int incrementerStock(@Param("id") Long id, @Param("pharmacieId") Long pharmacieId, @Param("quantite") int quantite);

    // ===== MÉTHODES DE RECHERCHE AVANCÉE =====
    @Query("SELECT m FROM Medpharmacie m WHERE m.pharmacieId = :pharmacieId AND LOWER(m.nom) LIKE LOWER(CONCAT('%', :nom, '%'))")
    List<Medpharmacie> searchByNomAndPharmacie(@Param("pharmacieId") Long pharmacieId, @Param("nom") String nom);

    // Vérifier le stock disponible pour une pharmacie spécifique
    @Query("SELECT m.quantite FROM Medpharmacie m WHERE m.id = :id AND m.pharmacieId = :pharmacieId")
    Integer getStockDisponible(@Param("id") Long id, @Param("pharmacieId") Long pharmacieId);

    // Recherche avec plusieurs critères
    @Query("SELECT m FROM Medpharmacie m WHERE m.pharmacieId = :pharmacieId " +
            "AND (:nom IS NULL OR LOWER(m.nom) LIKE LOWER(CONCAT('%', :nom, '%'))) " +
            "AND (:minPrix IS NULL OR m.prix >= :minPrix) " +
            "AND (:maxPrix IS NULL OR m.prix <= :maxPrix) " +
            "AND (:ordonnanceRequise IS NULL OR m.ordonnanceRequise = :ordonnanceRequise)")
    List<Medpharmacie> searchAdvanced(
            @Param("pharmacieId") Long pharmacieId,
            @Param("nom") String nom,
            @Param("minPrix") Double minPrix,
            @Param("maxPrix") Double maxPrix,
            @Param("ordonnanceRequise") Boolean ordonnanceRequise);

    // Mise à jour de la quantité vendue
    @Transactional
    @Modifying
    @Query("UPDATE Medpharmacie m SET m.quantiteVendue = m.quantiteVendue + :quantite WHERE m.id = :id AND m.pharmacieId = :pharmacieId")
    int incrementerQuantiteVendue(@Param("id") Long id, @Param("pharmacieId") Long pharmacieId, @Param("quantite") int quantite);

    // Trouver les médicaments les plus vendus pour une pharmacie
    @Query("SELECT m FROM Medpharmacie m WHERE m.pharmacieId = :pharmacieId ORDER BY m.quantiteVendue DESC")
    List<Medpharmacie> findPopularByPharmacieId(@Param("pharmacieId") Long pharmacieId);

    // Trouver les médicaments par liste d'IDs et pharmacie
    @Query("SELECT m FROM Medpharmacie m WHERE m.id IN :ids AND m.pharmacieId = :pharmacieId")
    List<Medpharmacie> findByIdInAndPharmacieId(@Param("ids") List<Long> ids, @Param("pharmacieId") Long pharmacieId);

    // Vérifier si un médicament existe pour une pharmacie spécifique
    @Query("SELECT COUNT(m) > 0 FROM Medpharmacie m WHERE m.nom = :nom AND m.pharmacieId = :pharmacieId")
    boolean existsByNomAndPharmacieId(@Param("nom") String nom, @Param("pharmacieId") Long pharmacieId);

    // Mise à jour du prix
    @Transactional
    @Modifying
    @Query("UPDATE Medpharmacie m SET m.prix = :prix WHERE m.id = :id AND m.pharmacieId = :pharmacieId")
    int updatePrix(@Param("id") Long id, @Param("pharmacieId") Long pharmacieId, @Param("prix") Double prix);

    // Mise à jour de la quantité
    @Transactional
    @Modifying
    @Query("UPDATE Medpharmacie m SET m.quantite = :quantite WHERE m.id = :id AND m.pharmacieId = :pharmacieId")
    int updateQuantite(@Param("id") Long id, @Param("pharmacieId") Long pharmacieId, @Param("quantite") Integer quantite);
}