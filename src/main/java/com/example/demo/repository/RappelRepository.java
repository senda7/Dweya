package com.example.demo.repository;

import com.example.demo.model.Rappel;
import com.example.demo.model.Medicament;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RappelRepository extends JpaRepository<Rappel, Long> {
    // Trouver tous les rappels liés à un médicament donné
    List<Rappel> findByMedicament_Utilisateur_Id(Long utilisateurId);
    // Trouver tous les rappels d'un utilisateur donné (via les médicaments)
    List<Rappel> findByMedicament(Medicament medicament);
}
