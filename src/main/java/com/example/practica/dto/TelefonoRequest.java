package com.example.practica.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Datos de un teléfono enviados en una solicitud")
public class TelefonoRequest {

    @Schema(description = "Identificador del registro", example = "18")
    private Long id;

    @NotBlank(message = "El tipo de teléfono es obligatorio")
    @Schema(description = "Tipo de contacto, por ejemplo PRINCIPAL", example = "PRINCIPAL")
    private String tipo;

    @NotBlank(message = "El teléfono es obligatorio")
    @Pattern(
            regexp = "^\\d{10}$",
            message = "El teléfono debe contener exactamente 10 dígitos"
    )
    @Schema(description = "Número telefónico de exactamente 10 dígitos", example = "7712345678")
    private String valor;
}