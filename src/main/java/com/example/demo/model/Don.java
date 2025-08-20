package com.example.demo.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class Don {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Don مرتبط بـ Medicament
    @ManyToOne
    @JoinColumn(name = "medicament_id")
    private Medicament medicament;

    // صورة للدواء المتبرع به
    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] image;

    // statut (EN_COURS, ACCEPTE, REFUSE)
    @Enumerated(EnumType.STRING)
    private StatutDon statut;

    // Getter & Setter
    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public Medicament getMedicament() { return medicament; }

    public void setMedicament(Medicament medicament) { this.medicament = medicament; }

    public byte[] getImage() { return image; }

    public void setImage(byte[] image) { this.image = image; }

    public StatutDon getStatut() { return statut; }

    public void setStatut(StatutDon statut) { this.statut = statut; }
}
