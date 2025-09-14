package com.example.demo.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "stock")
public class Medpharmacie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "date_peremption")
    private LocalDate datePeremption;

    @Column(nullable = false)
    private String nom;

    private String description;

    @Column(nullable = false)
    private Double prix;

    private Integer quantite;

    @Column(name = "quantite_vendue", nullable = false)
    private Integer quantiteVendue = 0;

    @Lob
    @Column(name = "photo_data", columnDefinition = "LONGTEXT")
    private String photoData;

    @Column(name = "ordonnance_requise")
    private boolean ordonnanceRequise;

    // Modification : utiliser l'ID de l'utilisateur pharmacie
    @Column(name = "pharmacie_id", nullable = false)
    private Long pharmacieId;

    // Relation ManyToOne vers Utilisateur (pharmacie)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pharmacie_id", insertable = false, updatable = false)
    private Utilisateur pharmacie;

    // Constructeurs
    public Medpharmacie() {
        this.quantiteVendue = 0;
    }

    public Medpharmacie(String nom, String description, Double prix, Integer quantite,
                        boolean ordonnanceRequise, Long pharmacieId) {
        this.nom = nom;
        this.description = description;
        this.prix = prix;
        this.quantite = quantite;
        this.ordonnanceRequise = ordonnanceRequise;
        this.pharmacieId = pharmacieId;
        this.quantiteVendue = 0;
    }

    // Getters et setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Double getPrix() { return prix; }
    public void setPrix(Double prix) { this.prix = prix; }

    public Integer getQuantite() { return quantite; }
    public void setQuantite(Integer quantite) { this.quantite = quantite; }

    public Integer getQuantiteVendue() { return quantiteVendue; }
    public void setQuantiteVendue(Integer quantiteVendue) { this.quantiteVendue = quantiteVendue; }

    public String getPhotoData() { return photoData; }
    public void setPhotoData(String photoData) { this.photoData = photoData; }

    public boolean isOrdonnanceRequise() { return ordonnanceRequise; }
    public void setOrdonnanceRequise(boolean ordonnanceRequise) { this.ordonnanceRequise = ordonnanceRequise; }

    public Long getPharmacieId() { return pharmacieId; }
    public void setPharmacieId(Long pharmacieId) { this.pharmacieId = pharmacieId; }

    public Utilisateur getPharmacie() { return pharmacie; }
    public void setPharmacie(Utilisateur pharmacie) { this.pharmacie = pharmacie; }
    public LocalDate getDatePeremption() { return datePeremption; }
    public void setDatePeremption(LocalDate datePeremption) { this.datePeremption = datePeremption; }
}