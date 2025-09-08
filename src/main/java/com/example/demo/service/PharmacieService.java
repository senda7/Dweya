package com.example.demo.service;

import com.example.demo.model.Utilisateur;
import com.example.demo.repository.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PharmacieService {

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    public Optional<Utilisateur> findById(Long id) {
        return utilisateurRepository.findById(id);
    }

    public List<Utilisateur> findAllPharmacies() {
        return utilisateurRepository.findByTypeUtilisateur(Utilisateur.TypeUtilisateur.PHARMACIE);
    }
}