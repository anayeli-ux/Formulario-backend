package com.example.practica.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CorreoRequest {

    private Long id;

    @NotBlank(message = "El tipo de correo es obligatorio")
    private String tipo;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo no tiene un formato válido")
    @Size(
            max = 255,
            message = "El correo no puede superar los 255 caracteres"
    )
    private String valor;
}