package com.example.practica.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "direcciones", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Direccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String direccion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "codigo_postal", nullable = false)
    private CodigoPostal codigoPostal;

    @Column(length = 45)
    private String categoria;

}