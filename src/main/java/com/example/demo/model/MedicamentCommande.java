package com.example.demo.model;

import jakarta.persistence.*;

@Entity
public class MedicamentCommande {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;
    private double prix;
    private int quantite;

    // ✅ Relation vers Commande
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commande_id", nullable = false)
    private Commande commande;

    public MedicamentCommande() {}

    public MedicamentCommande(String nom, double prix, int quantite, Commande commande) {
        this.nom = nom;
        this.prix = prix;
        this.quantite = quantite;
        this.commande = commande;
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public double getPrix() { return prix; }
    public void setPrix(double prix) { this.prix = prix; }

    public int getQuantite() { return quantite; }
    public void setQuantite(int quantite) { this.quantite = quantite; }

    public Commande getCommande() { return commande; }
    public void setCommande(Commande commande) { this.commande = commande; }
}
