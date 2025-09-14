package com.example.demo.repository;

import com.example.demo.model.Utilisateur;
import com.example.demo.model.Utilisateur.TypeUtilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {

    // Méthodes de recherche
    Utilisateur findByEmailAndMotDePasse(String email, String motDePasse);

    List<Utilisateur> findByTypeUtilisateur(TypeUtilisateur typeUtilisateur);

    List<Utilisateur> findByTypeUtilisateurAndEtat(TypeUtilisateur typeUtilisateur, boolean etat);

    List<Utilisateur> findByTypeUtilisateurAndEtatAndVille(
            TypeUtilisateur typeUtilisateur,
            boolean etat,
            String ville
    );

    Utilisateur findByEmail(String email);

    List<Utilisateur> findByRoleId(Long roleId);

    boolean existsByEmail(String email);

    List<Utilisateur> findByNomContainingIgnoreCase(String nom);

    List<Utilisateur> findByNomPharmacieContainingIgnoreCase(String nomPharmacie);

    List<Utilisateur> findByVille(String ville);

    List<Utilisateur> findByVilleContainingIgnoreCase(String ville);

    List<Utilisateur> findByEtat(boolean etat);

    // Requêtes personnalisées
    @Query("SELECT u FROM Utilisateur u WHERE u.typeUtilisateur = 'PHARMACIE' AND u.etat = true " +
            "AND EXISTS (SELECT m FROM Medpharmacie m WHERE m.pharmacieId = u.id AND m.quantite > 0)")
    List<Utilisateur> findPharmaciesAvecStock();

    @Query("SELECT u FROM Utilisateur u WHERE u.typeUtilisateur = 'PHARMACIE' AND u.etat = true AND u.ville = :ville " +
            "AND EXISTS (SELECT m FROM Medpharmacie m WHERE m.pharmacieId = u.id AND m.nom LIKE %:medicament% AND m.quantite > 0)")
    List<Utilisateur> findPharmaciesParVilleAvecMedicament(
            @Param("ville") String ville,
            @Param("medicament") String medicament
    );

    @Query("SELECT u FROM Utilisateur u WHERE LOWER(u.nom) LIKE LOWER(CONCAT('%', :recherche, '%')) " +
            "OR LOWER(u.nomPharmacie) LIKE LOWER(CONCAT('%', :recherche, '%'))")
    List<Utilisateur> findByNomOrNomPharmacieContainingIgnoreCase(@Param("recherche") String recherche);

    // Méthodes de comptage
    long countByTypeUtilisateur(TypeUtilisateur typeUtilisateur);

    long countByTypeUtilisateurAndEtat(TypeUtilisateur typeUtilisateur, boolean etat);

    Optional<Utilisateur> findFirstByEmailAndMotDePasse(String email, String motDePasse);
}