package com.example.practica.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Datos de un correo electrónico enviados en una solicitud")
public class CorreoRequest {

    @Schema(description = "Identificador del registro", example = "18")
    private Long id;

    @NotBlank(message = "El tipo de correo es obligatorio")
    @Schema(description = "Tipo de contacto, por ejemplo PRINCIPAL", example = "PRINCIPAL")
    private String tipo;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo no tiene un formato válido")
    @Size(
            max = 255,
            message = "El correo no puede superar los 255 caracteres"
    )
    @Schema(description = "Correo electrónico válido", example = "ana.prueba2026@example.com")
    private String valor;
}