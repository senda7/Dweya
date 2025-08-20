package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.List;

@Entity
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Exemple : "admin", "pharmacie", "utilisateur"
    private String nom;

    @OneToMany(mappedBy = "role", cascade = CascadeType.ALL)
    @JsonIgnore // باش ما يدورش مع Utilisateur ويعمل StackOverflow
    private List<Utilisateur> utilisateurs;

    // ======== Constructeur par défaut ========
    public Role() {
    }

    public Role(String nom) {
        this.nom = nom;
    }

    // ======== Getters & Setters ========
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public List<Utilisateur> getUtilisateurs() {
        return utilisateurs;
    }

    public void setUtilisateurs(List<Utilisateur> utilisateurs) {
        this.utilisateurs = utilisateurs;
    }

    // ======== toString() ========
    @Override
    public String toString() {
        return "Role{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                '}';
        // intentionally ما حطيناش utilisateurs باش ما ندخلوش في الحلقة
    }
}
