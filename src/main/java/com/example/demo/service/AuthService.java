package com.example.demo.service;

import com.example.demo.model.Utilisateur;
import com.example.demo.repository.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    public Utilisateur login(String email, String motDePasse) {

        return utilisateurRepository.findByEmailAndMotDePasse(email, motDePasse);

    }
}
