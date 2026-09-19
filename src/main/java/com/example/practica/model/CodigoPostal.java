package com.example.practica.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "codigos_postales", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CodigoPostal {

    @Id
    @Column(
            name = "codigo_postal",
            length = 5,
            nullable = false
    )
    private String codigoPostal;

    @OneToMany(mappedBy = "codigoPostal")
    @Builder.Default
    private List<Direccion> direcciones = new ArrayList<>();
}