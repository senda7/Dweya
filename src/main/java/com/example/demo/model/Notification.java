package com.example.demo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long pharmacieId;

    @Column(nullable = false)
    private String message;

    @Column(nullable = false)
    private String type; // STOCK_ALERT, ORDER_ALERT, etc.

    @Column(nullable = false)
    private boolean isRead = false;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getPharmacieId() { return pharmacieId; }
    public void setPharmacieId(Long pharmacieId) { this.pharmacieId = pharmacieId; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}