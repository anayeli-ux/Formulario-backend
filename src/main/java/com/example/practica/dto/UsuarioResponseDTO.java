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

    private LocalDate fechaNacimiento;

    // null = activo
    // fecha/hora = dado de baja
    private LocalDateTime fechaBaja;

    private String rol;

    @Builder.Default
    private List<TelefonoResponseDTO> telefonos =
            new ArrayList<>();

    @Builder.Default
    private List<CorreoResponseDTO> correos =
            new ArrayList<>();

    @Builder.Default
    private List<DireccionResponseDTO> direcciones =
            new ArrayList<>();
}