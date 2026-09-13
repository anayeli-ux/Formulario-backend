package com.example.practica.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioResponseDTO {

    private Long id;

    private String nombre;

    private String primerApellido;

    private String segundoApellido;

    private String telefono;

    private String codigoPostal;

    private String estado;

    private String municipio;

    private String direccion;

    private LocalDate fechaNacimiento;

    private String animalFavorito;

    private boolean activo;
}