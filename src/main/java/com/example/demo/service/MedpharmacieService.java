
package com.example.demo.service;

import com.example.demo.model.Medpharmacie;
import com.example.demo.repository.MedpharmacieRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    public Medpharmacie saveMedicament(Medpharmacie medicament) {
        return medpharmacieRepository.save(medicament);
    }

    public void deleteMedicament(Long id) {
        medpharmacieRepository.deleteById(id);
    }
}
