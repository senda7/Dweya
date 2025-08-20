package com.example.demo.repository;

import com.example.demo.model.Medicament;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MedicamentRepository extends JpaRepository<Medicament, Long> {

    List<Medicament> findByUtilisateurId(Long utilisateurId);
    // récupérer que les médicaments liés à utilisateur spécifique


}
