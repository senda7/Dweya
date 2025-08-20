package com.example.demo.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.web.multipart.MultipartFile;

@Entity
public class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ===== Champs communs =====
    private String nom;
    private String prenom;
    private String genre;
    private LocalDate dateNaissance;
    private String telephone;
    private String email;
    private String adresse;
    private String ville;
    private String motDePasse;



    // ===== Type d'utilisateur =====
    @Enumerated(EnumType.STRING)
    private TypeUtilisateur typeUtilisateur;

    @Column(nullable = false)
    private boolean etat;

    // ===== Champs spécifiques à la pharmacie =====
    private String nomPharmacie;
    private String numeroLicence;
    private String numeroOrdre;

    // ===== Fichiers =====
    @Transient
    private MultipartFile registreCommerceFile;

    @Transient
    private MultipartFile cinPharmacienFile;

    @Transient
    private MultipartFile autorisationMinistereFile;

    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] registreCommerce;

    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] cinPharmacien;

    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] autorisationMinistere;

    // ===== Relation avec le rôle =====
    @ManyToOne
    @JoinColumn(name = "idRole")
    @JsonIgnore
    private Role role;

    @OneToMany(mappedBy = "utilisateur", cascade = CascadeType.ALL)
    private List<Medicament> medicaments;

    public enum TypeUtilisateur {
        ADMIN,
        UTILISATEUR,
        PHARMACIE
    }

    // ===== Constructeur par défaut =====
    public Utilisateur() {
        this.setTypeUtilisateur(TypeUtilisateur.UTILISATEUR); // actif par défaut
    }

    // ===== Getters & Setters =====
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    public LocalDate getDateNaissance() { return dateNaissance; }
    public void setDateNaissance(LocalDate dateNaissance) { this.dateNaissance = dateNaissance; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }

    public String getVille() { return ville; }
    public void setVille(String ville) { this.ville = ville; }

    public String getMotDePasse() { return motDePasse; }
    public void setMotDePasse(String motDePasse) { this.motDePasse = motDePasse; }



    public TypeUtilisateur getTypeUtilisateur() { return typeUtilisateur; }
    public void setTypeUtilisateur(TypeUtilisateur typeUtilisateur) {
        this.typeUtilisateur = typeUtilisateur;
        if (typeUtilisateur == TypeUtilisateur.PHARMACIE) {
            this.etat = false; // compte désactivé
        } else {
            this.etat = true;  // actif pour utilisateur/admin
        }
    }

    public boolean isEtat() { return etat; }
    public void setEtat(boolean etat) { this.etat = etat; }

    public String getNomPharmacie() { return nomPharmacie; }
    public void setNomPharmacie(String nomPharmacie) { this.nomPharmacie = nomPharmacie; }

    public String getNumeroLicence() { return numeroLicence; }
    public void setNumeroLicence(String numeroLicence) { this.numeroLicence = numeroLicence; }

    public String getNumeroOrdre() { return numeroOrdre; }
    public void setNumeroOrdre(String numeroOrdre) { this.numeroOrdre = numeroOrdre; }

    public MultipartFile getRegistreCommerceFile() { return registreCommerceFile; }
    public void setRegistreCommerceFile(MultipartFile registreCommerceFile) { this.registreCommerceFile = registreCommerceFile; }

    public MultipartFile getCinPharmacienFile() { return cinPharmacienFile; }
    public void setCinPharmacienFile(MultipartFile cinPharmacienFile) { this.cinPharmacienFile = cinPharmacienFile; }

    public MultipartFile getAutorisationMinistereFile() { return autorisationMinistereFile; }
    public void setAutorisationMinistereFile(MultipartFile autorisationMinistereFile) { this.autorisationMinistereFile = autorisationMinistereFile; }

    public byte[] getRegistreCommerce() { return registreCommerce; }
    public void setRegistreCommerce(byte[] registreCommerce) { this.registreCommerce = registreCommerce; }

    public byte[] getCinPharmacien() { return cinPharmacien; }
    public void setCinPharmacien(byte[] cinPharmacien) { this.cinPharmacien = cinPharmacien; }

    public byte[] getAutorisationMinistere() { return autorisationMinistere; }
    public void setAutorisationMinistere(byte[] autorisationMinistere) { this.autorisationMinistere = autorisationMinistere; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public List<Medicament> getMedicaments() { return medicaments; }
    public void setMedicaments(List<Medicament> medicaments) { this.medicaments = medicaments; }
}
