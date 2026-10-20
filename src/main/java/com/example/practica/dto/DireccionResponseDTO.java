package com.example.practica.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Datos de una dirección en la respuesta")
public class DireccionResponseDTO {

    @Schema(description = "Identificador del registro", example = "18")
    private Long id;
    @Schema(description = "Tipo de contacto, por ejemplo PRINCIPAL", example = "PRINCIPAL")
    private String tipo;
    @Schema(description = "Valor del contacto")
    private String valor;
    @Schema(description = "Código postal de cinco dígitos", example = "42000")
    private String codigoPostal;
}