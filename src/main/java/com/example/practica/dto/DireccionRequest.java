package com.example.practica.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DireccionRequest {

    @NotBlank(message = "El tipo de dirección es obligatorio")
    private String tipo;

    @NotBlank(message = "La dirección es obligatoria")
    @Size(
            max = 255,
            message = "La dirección no puede superar los 255 caracteres"
    )
    private String valor;

    @NotBlank(message = "El código postal de la dirección es obligatorio")
    @Pattern(
            regexp = "^\\d{5}$",
            message = "El código postal debe contener exactamente 5 dígitos"
    )
    private String codigoPostal;
}