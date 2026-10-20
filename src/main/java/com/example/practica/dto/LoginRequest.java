package com.example.practica.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Credenciales para iniciar sesión")
public class LoginRequest {

    @NotBlank(message = "El usuario es obligatorio")
    @Schema(description = "Correo o identificador usado para iniciar sesión", example = "ana.prueba2026@example.com")
    private String usuario;

    @NotBlank(message = "La contraseña es obligatoria")
    @Schema(description = "Contraseña para autenticación", format = "password", accessMode = Schema.AccessMode.WRITE_ONLY)
    private String password;
}