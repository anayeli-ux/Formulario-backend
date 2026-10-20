package com.example.practica.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Datos de una dirección enviados en una solicitud")
public class DireccionRequest {

    @Schema(description = "Identificador del registro", example = "18")
    private Long id;

    @NotBlank(message = "El tipo de dirección es obligatorio")
    @Schema(description = "Tipo de contacto, por ejemplo PRINCIPAL", example = "PRINCIPAL")
    private String tipo;

    @NotBlank(message = "La dirección es obligatoria")
    @Size(
            max = 255,
            message = "La dirección no puede superar los 255 caracteres"
    )
    @Schema(description = "Texto de la dirección", example = "Calle Primavera 123")
    private String valor;

    @NotBlank(message = "El código postal de la dirección es obligatorio")
    @Pattern(
            regexp = "^\\d{5}$",
            message = "El código postal debe contener exactamente 5 dígitos"
    )
    @Schema(description = "Código postal de cinco dígitos", example = "42000")
    private String codigoPostal;
}