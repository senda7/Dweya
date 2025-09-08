package com.example.demo.service;

import com.example.demo.model.Commande;
import com.example.demo.repository.CommandeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CommandeService {

    @Autowired
    private CommandeRepository commandeRepository;

    // ======= Trouver toutes les commandes d'une pharmacie avec leurs médicaments =======
    public List<Commande> findByPharmacieIdWithMedicaments(Long pharmacieId) {
        return commandeRepository.findByPharmacieIdWithMedicaments(pharmacieId);
    }

    public List<Commande> findByPharmacieId(Long pharmacieId) {
        return commandeRepository.findByPharmacieId(pharmacieId);
    }

    public List<Commande> findByPharmacieIdAndStatut(Long pharmacieId, String statut) {
        return commandeRepository.findByPharmacieIdAndStatut(pharmacieId, statut);
    }

    public List<Commande> findAll() {
        return commandeRepository.findAll();
    }

    public List<Commande> findCommandesEnAttente(Long pharmacieId) {
        return commandeRepository.findCommandesEnAttente(pharmacieId);
    }

    @Transactional
    public void validerCommande(Long commandeId, Long pharmacieId) {
        Commande commande = commandeRepository.findByIdAndPharmacieId(commandeId, pharmacieId)
                .orElseThrow(() -> new RuntimeException("Commande non trouvée"));
        commande.setStatut("VALIDEE");
        commandeRepository.save(commande);
    }

    @Transactional
    public void annulerCommande(Long commandeId, Long pharmacieId) {
        Commande commande = commandeRepository.findByIdAndPharmacieId(commandeId, pharmacieId)
                .orElseThrow(() -> new RuntimeException("Commande non trouvée"));
        commande.setStatut("ANNULEE");
        commandeRepository.save(commande);
    }

    public Optional<Commande> findByIdAndPharmacieId(Long id, Long pharmacieId) {
        return commandeRepository.findByIdAndPharmacieId(id, pharmacieId);
    }

    @Transactional
    public Commande save(Commande commande) {
        // ✅ Vérification et association des médicaments
        if (commande.getMedicaments() != null) {
            commande.getMedicaments().forEach(med -> med.setCommande(commande));
        }
        return commandeRepository.save(commande);
    }
}
