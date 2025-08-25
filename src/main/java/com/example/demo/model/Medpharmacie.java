package com.example.demo.model;

import jakarta.persistence.*;

@Entity
@Table(name = "stock")
public class Medpharmacie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    private String description;

    @Column(nullable = false)
    private Double prix;

    private Integer quantite;

    @Lob // Annotation importante pour les gros objets
    @Column(name = "photo_data", columnDefinition = "LONGTEXT") // LONGTEXT pour MySQL
    private String photoData; // Changé de photoPath à photoData

    @Column(name = "ordonnance_requise")
    private boolean ordonnanceRequise;

    @Column(name = "pharmacie_id", nullable = false)
    private Long pharmacieId;

    // Constructeurs
    public Medpharmacie() {}

    public Medpharmacie(String nom, String description, Double prix, Integer quantite,
                        boolean ordonnanceRequise, Long pharmacieId) {
        this.nom = nom;
        this.description = description;
        this.prix = prix;
        this.quantite = quantite;
        this.ordonnanceRequise = ordonnanceRequise;
        this.pharmacieId = pharmacieId;
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

    public String getPhotoData() { return photoData; }
    public void setPhotoData(String photoData) { this.photoData = photoData; }

    public boolean isOrdonnanceRequise() { return ordonnanceRequise; }
    public void setOrdonnanceRequise(boolean ordonnanceRequise) { this.ordonnanceRequise = ordonnanceRequise; }

    public Long getPharmacieId() { return pharmacieId; }
    public void setPharmacieId(Long pharmacieId) { this.pharmacieId = pharmacieId; }
}