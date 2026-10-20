package com.example.practica.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Datos de un correo electrónico en la respuesta")
public class CorreoResponseDTO {

    @Schema(description = "Identificador del registro", example = "18")
    private Long id;
    @Schema(description = "Tipo de contacto, por ejemplo PRINCIPAL", example = "PRINCIPAL")
    private String tipo;
    @Schema(description = "Valor del contacto")
    private String valor;
}