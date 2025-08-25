package com.example.demo.service;

import com.example.demo.model.Medpharmacie;
import com.example.demo.model.Utilisateur;
import com.example.demo.repository.MedpharmacieRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MedpharmacieService {

    @Autowired
    private MedpharmacieRepository medpharmacieRepository;

    // 🔹 Tous les médicaments d'une pharmacie
    public List<Medpharmacie> getMedicamentsByPharmacie(Utilisateur pharmacie) {
        return medpharmacieRepository.findByPharmacie(pharmacie);
    }

    // 🔹 Rechercher par nom dans la pharmacie
    public List<Medpharmacie> searchMedicamentsByPharmacie(Utilisateur pharmacie, String nom) {
        return medpharmacieRepository.findByPharmacieAndNomContainingIgnoreCase(pharmacie, nom);
    }

    // 🔹 Récupérer un médicament par ID et pharmacie
    public Optional<Medpharmacie> getMedicamentByIdAndPharmacie(Long id, Utilisateur pharmacie) {
        return medpharmacieRepository.findByIdAndPharmacie(id, pharmacie);
    }

    // 🔹 Ajouter ou modifier un médicament
    public Medpharmacie saveMedicament(Medpharmacie medicament) {
        return medpharmacieRepository.save(medicament);
    }

    // 🔹 Supprimer un médicament
    public void deleteMedicament(Medpharmacie medicament) {
        medpharmacieRepository.delete(medicament);
    }
}
