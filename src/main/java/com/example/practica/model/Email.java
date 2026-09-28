package com.example.practica.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "emails", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Email {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tipo", nullable = false, length = 50)
    private String tipo;

    @Column(name = "valor", nullable = false, length = 255)
    private String valor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
}