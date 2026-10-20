package com.example.practica.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
@Schema(description = "Resumen de la sesión del usuario")
public class UsuarioSesionDTO {
    @Schema(description = "Identificador del registro", example = "18")
    Long id;
    @Schema(description = "Nombre", example = "Ana")
    String nombre;
    @Schema(description = "Primer apellido", example = "Garcia")
    String primerApellido;
    @Schema(description = "Correo electrónico del usuario", example = "ana.prueba2026@example.com")
    String email;
    @Schema(description = "Rol del usuario: USER o ADMIN", allowableValues = {"USER", "ADMIN"})
    String rol;
}