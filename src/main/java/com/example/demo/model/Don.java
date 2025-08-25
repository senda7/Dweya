package com.example.demo.model;

import jakarta.persistence.*;

@Entity
public class Don {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Don est associé à Medicament
    @ManyToOne
    @JoinColumn(name = "medicament_id")
    private Medicament medicament;

    // Image du médicament donné
    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] image;
    @ManyToOne
    @JoinColumn(name = "pharmacie_id")
    private Utilisateur pharmacie;

    @Column(columnDefinition = "TEXT")
    private String description;

    // statut (EN_COURS, ACCEPTE, REFUSE)
    @Enumerated(EnumType.STRING)
    private StatutDon statut;

    @Column(length = 50)
    private String codeDemande; // pour stocker le code unique envoyé à l'utilisateur

    // Getter & Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Medicament getMedicament() { return medicament; }
    public void setMedicament(Medicament medicament) { this.medicament = medicament; }

    public byte[] getImage() { return image; }
    public void setImage(byte[] image) { this.image = image; }

    public StatutDon getStatut() { return statut; }
    public void setStatut(StatutDon statut) { this.statut = statut; }

    public Utilisateur getPharmacie() { return pharmacie; }
    public void setPharmacie(Utilisateur pharmacie) { this.pharmacie = pharmacie; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCodeDemande() { return codeDemande; }
    public void setCodeDemande(String codeDemande) { this.codeDemande = codeDemande; }
}
