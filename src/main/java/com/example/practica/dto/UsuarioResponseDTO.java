package com.example.practica.dto;

import lombok.*;

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

    private String telefono;

    private String codigoPostal;

    private String estado;

    private String municipio;

    private String direccion;

    private LocalDate fechaNacimiento;

    private String email;

    private boolean activo;

    private String rol;
}