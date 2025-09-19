package com.example.demo.repository;// Dans NotificationUtilisateurRepository.java
import com.example.demo.model.NotificationUtilisateur;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationUtilisateurRepository extends JpaRepository<NotificationUtilisateur, Long> {

    // Récupérer les notifications non lues
    List<NotificationUtilisateur> findByUtilisateurIdAndLuFalse(Long utilisateurId);

    // Compter combien de notifications non lues
    int countByUtilisateurIdAndLuFalse(Long utilisateurId);

    // Nouvelle méthode avec pagination - CORRIGÉE (sans corps de méthode)
    @Query("SELECT n FROM NotificationUtilisateur n WHERE n.utilisateur.id = :userId ORDER BY n.date DESC")
    List<NotificationUtilisateur> findLatestByUtilisateurId(@Param("userId") Long userId, Pageable pageable);
}