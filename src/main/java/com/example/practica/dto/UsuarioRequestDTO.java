package com.example.practica.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioRequestDTO {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 255, message = "El nombre debe tener entre 2 y 255 caracteres")
    private String nombre;

    @NotBlank(message = "El primer apellido es obligatorio")
    @Size(max = 255, message = "El primer apellido no puede superar los 255 caracteres")
    private String primerApellido;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, max = 255, message = "La contraseña debe tener al menos 8 caracteres")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z\\d]).{8,}$",
            message = "La contraseña debe contener al menos una letra, un número y un símbolo"
    )
    private String password;

    @NotBlank(message = "El teléfono es obligatorio")
    @Pattern(
            regexp = "^\\d{10}$",
            message = "El teléfono debe contener exactamente 10 dígitos"
    )
    private String telefono;

    @NotBlank(message = "El código postal es obligatorio")
    @Pattern(
            regexp = "^\\d{5}$",
            message = "El código postal debe contener exactamente 5 dígitos"
    )
    private String codigoPostal;

    @NotBlank(message = "La dirección es obligatoria")
    @Size(max = 255, message = "La dirección no puede superar los 255 caracteres")
    private String direccion;

    @NotNull(message = "La fecha de nacimiento es obligatoria")
    @Past(message = "La fecha de nacimiento debe ser anterior a la fecha actual")
    private LocalDate fechaNacimiento;

    @NotBlank(message = "El correo electrónico es obligatorio")
    @Email(message = "El correo electrónico no tiene un formato válido")
    @Size(max = 255, message = "El correo electrónico no puede superar los 255 caracteres")
    private String email;
}