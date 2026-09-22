package com.example.practica.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TelefonoRequest {

    @NotBlank(message = "El tipo de teléfono es obligatorio")
    private String tipo;

    @NotBlank(message = "El teléfono es obligatorio")
    @Pattern(
            regexp = "^\\d{10}$",
            message = "El teléfono debe contener exactamente 10 dígitos"
    )
    private String valor;
}