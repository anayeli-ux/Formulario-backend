package com.example.practica.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioResponseDTO {

    private Long id;

    private String nombre;

    private String primerApellido;

    // Datos principales
    private String telefono;

    private String codigoPostal;

    private String estado;

    private String municipio;

    private String direccion;

    private LocalDate fechaNacimiento;

    private String email;

    // null = activo
    // fecha/hora = dado de baja
    private LocalDateTime fechaBaja;

    private String rol;

    // Todos los teléfonos
    @Builder.Default
    private List<TelefonoResponseDTO> telefonos =
            new ArrayList<>();

    // Todas las direcciones
    @Builder.Default
    private List<DireccionResponseDTO> direcciones =
            new ArrayList<>();
}