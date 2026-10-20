package com.example.practica.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
@Schema(description = "Resumen de usuario para listados")
public class UsuarioResumenDTO {
    @Schema(description = "Identificador del registro", example = "18")
    Long id;
    @Schema(description = "Nombre", example = "Ana")
    String nombre;
    @Schema(description = "Primer apellido", example = "Garcia")
    String primerApellido;
    @Schema(description = "Rol del usuario: USER o ADMIN", allowableValues = {"USER", "ADMIN"})
    String rol;
    @Schema(description = "Teléfono mostrado en el resumen", example = "7712345678")
    String telefono;
    @Schema(description = "Correo mostrado en el resumen", example = "ana.prueba2026@example.com")
    String correo;
    @Schema(description = "Código postal de cinco dígitos", example = "42000")
    String codigoPostal;
}