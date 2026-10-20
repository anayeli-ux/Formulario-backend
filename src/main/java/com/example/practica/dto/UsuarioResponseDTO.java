package com.example.practica.dto;

import io.swagger.v3.oas.annotations.media.Schema;

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
@Schema(description = "Datos del usuario devueltos por la API")
public class UsuarioResponseDTO {

    @Schema(description = "Identificador del registro", example = "18")
    private Long id;

    @Schema(description = "Nombre", example = "Ana")
    private String nombre;

    @Schema(description = "Primer apellido", example = "Garcia")
    private String primerApellido;

    @Schema(description = "Fecha de nacimiento (anterior a la fecha actual)", example = "2000-10-20")
    private LocalDate fechaNacimiento;

    // null = activo
    // fecha/hora = dado de baja
    @Schema(description = "Fecha y hora de baja lógica; null si está activo")
    private LocalDateTime fechaBaja;

    @Schema(description = "Rol del usuario: USER o ADMIN", allowableValues = {"USER", "ADMIN"})
    private String rol;

    @Builder.Default
    @Schema(description = "Teléfonos asociados al usuario")
    private List<TelefonoResponseDTO> telefonos =
            new ArrayList<>();

    @Builder.Default
    @Schema(description = "Correos electrónicos asociados al usuario")
    private List<CorreoResponseDTO> correos =
            new ArrayList<>();

    @Builder.Default
    @Schema(description = "Direcciones asociadas al usuario")
    private List<DireccionResponseDTO> direcciones =
            new ArrayList<>();
}