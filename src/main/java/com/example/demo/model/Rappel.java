package com.example.demo.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
public class Rappel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Medicament medicament;

    private int frequence;

    @ElementCollection
    private List<String> heuresPrise;



    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Medicament getMedicament() {
        return medicament;
    }

    public void setMedicament(Medicament medicament) {
        this.medicament = medicament;
    }

    public int getFrequence() {
        return frequence;
    }

    public void setFrequence(int frequence) {
        this.frequence = frequence;
    }

    public List<String> getHeuresPrise() {
        return heuresPrise;
    }

    public void setHeuresPrise(List<String> heuresPrise) {
        this.heuresPrise = heuresPrise;
    }
}
