package com.example.practica.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Datos básicos del usuario autenticado")
public class UsuarioLoginDTO {

    @Schema(description = "Identificador del registro", example = "18")
    private Long id;

    @Schema(description = "Correo electrónico del usuario", example = "ana.prueba2026@example.com")
    private String email;

    @Schema(description = "Rol del usuario: USER o ADMIN", allowableValues = {"USER", "ADMIN"})
    private String rol;

    @Schema(description = "Nombre", example = "Ana")
    private String nombre;

    @Schema(description = "Primer apellido", example = "Garcia")
    private String primerApellido;
}