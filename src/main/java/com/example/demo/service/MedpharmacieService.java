package com.example.demo.service;

import com.example.demo.model.Medpharmacie;
import com.example.demo.repository.MedpharmacieRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class MedpharmacieService {

    @Autowired
    private MedpharmacieRepository medpharmacieRepository;

    public List<Medpharmacie> getMedicamentsByPharmacie(Long pharmacieId) {
        return medpharmacieRepository.findByPharmacieId(pharmacieId);
    }

    public List<Medpharmacie> searchMedicamentsByPharmacie(Long pharmacieId, String nom) {
        return medpharmacieRepository.findByPharmacieIdAndNomContainingIgnoreCase(pharmacieId, nom);
    }

    public Optional<Medpharmacie> getMedicamentById(Long id) {
        return medpharmacieRepository.findById(id);
    }

    // Nouvelle méthode pour récupérer un médicament par ID et pharmacie
    public Optional<Medpharmacie> getMedicamentByIdAndPharmacie(Long id, Long pharmacieId) {
        return medpharmacieRepository.findByIdAndPharmacieId(id, pharmacieId);
    }

    public Medpharmacie saveMedicament(Medpharmacie medicament) {
        return medpharmacieRepository.save(medicament);
    }

    public void deleteMedicament(Long id) {
        medpharmacieRepository.deleteById(id);
    }

    // Nouvelle méthode pour supprimer avec vérification de la pharmacie
    public boolean deleteMedicamentWithCheck(Long id, Long pharmacieId) {
        if (medpharmacieRepository.existsByIdAndPharmacieId(id, pharmacieId)) {
            medpharmacieRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // Nouvelles méthodes

    public List<Medpharmacie> getMedicamentsEnStockByPharmacie(Long pharmacieId) {
        return medpharmacieRepository.findByPharmacieIdAndQuantiteGreaterThan(pharmacieId, 0);
    }

    public List<Medpharmacie> getMedicamentsHorsStockByPharmacie(Long pharmacieId) {
        return medpharmacieRepository.findByPharmacieIdAndQuantite(pharmacieId, 0);
    }

    @Transactional
    public boolean decrementerStock(Long medicamentId, Long pharmacieId, int quantite) {
        int rowsAffected = medpharmacieRepository.decrementerStock(medicamentId, pharmacieId, quantite);
        return rowsAffected > 0;
    }

    @Transactional
    public void incrementerStock(Long medicamentId, Long pharmacieId, int quantite) {
        medpharmacieRepository.incrementerStock(medicamentId, pharmacieId, quantite);
    }

    // Méthodes temporairement commentées car non implémentées dans le repository
    /*
    public List<Medpharmacie> getMedicamentsPopulaires(Long pharmacieId) {
        return medpharmacieRepository.findPopularByPharmacieId(pharmacieId);
    }

    @Transactional
    public void incrementerQuantiteVendue(Long medicamentId, int quantite) {
        medpharmacieRepository.incrementerQuantiteVendue(medicamentId, quantite);
    }

    public List<Medpharmacie> searchAdvanced(Long pharmacieId, String nom, Double minPrix,
                                             Double maxPrix, Boolean ordonnanceRequise) {
        return medpharmacieRepository.searchAdvanced(pharmacieId, nom, minPrix, maxPrix, ordonnanceRequise);
    }

    public List<Medpharmacie> findByIds(List<Long> ids) {
        return medpharmacieRepository.findByIdIn(ids);
    }
    */

    public Medpharmacie save(Medpharmacie medicament) {
        return medpharmacieRepository.save(medicament);
    }

    public boolean verifierStockSuffisant(Long medicamentId, int quantiteDemandee) {
        Optional<Medpharmacie> medicamentOpt = medpharmacieRepository.findById(medicamentId);
        if (medicamentOpt.isPresent()) {
            Medpharmacie medicament = medicamentOpt.get();
            return medicament.getQuantite() != null && medicament.getQuantite() >= quantiteDemandee;
        }
        return false;
    }

    // Nouvelle méthode avec vérification de la pharmacie
    public boolean verifierStockSuffisant(Long medicamentId, Long pharmacieId, int quantiteDemandee) {
        Optional<Medpharmacie> medicamentOpt = medpharmacieRepository.findByIdAndPharmacieId(medicamentId, pharmacieId);
        if (medicamentOpt.isPresent()) {
            Medpharmacie medicament = medicamentOpt.get();
            return medicament.getQuantite() != null && medicament.getQuantite() >= quantiteDemandee;
        }
        return false;
    }

    // Méthodes supplémentaires utiles
    public List<Medpharmacie> getAllMedicaments() {
        return medpharmacieRepository.findAll();
    }

    public List<Medpharmacie> searchMedicaments(String nom) {
        return medpharmacieRepository.findByNomContainingIgnoreCase(nom);
    }

    public long countMedicamentsByPharmacie(Long pharmacieId) {
        return medpharmacieRepository.countByPharmacieId(pharmacieId);
    }

    public long countMedicamentsEnRuptureByPharmacie(Long pharmacieId) {
        return medpharmacieRepository.countByPharmacieIdAndQuantiteLessThan(pharmacieId, 1);
    }

    // Nouvelle méthode pour vérifier l'existence
    public boolean existsByIdAndPharmacieId(Long id, Long pharmacieId) {
        return medpharmacieRepository.existsByIdAndPharmacieId(id, pharmacieId);
    }
    // Dans MedpharmacieService.java
    @Autowired
    private NotificationService notificationService;

    public void checkStockLevels(Medpharmacie medicament, Long pharmacieId) {
        if (medicament.getQuantite() <= 0) {
            notificationService.createStockAlert(medicament, pharmacieId);
        }
    }
}