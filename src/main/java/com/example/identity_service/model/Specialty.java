package com.example.identity_service.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "specialties")
@Data
public class Specialty {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nome;
}
