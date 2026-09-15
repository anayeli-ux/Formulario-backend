package com.example.practica.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioRequestDTO {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(
            min = 2,
            max = 50,
            message = "El nombre debe tener entre 2 y 50 caracteres"
    )
    private String nombre;

    @NotBlank(message = "El primer apellido es obligatorio")
    @Size(
            max = 50,
            message = "El primer apellido no puede superar 50 caracteres"
    )
    private String primerApellido;

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
    @Size(
            max = 150,
            message = "La dirección no puede superar 150 caracteres"
    )
    private String direccion;

    @NotNull(message = "La fecha de nacimiento es obligatoria")
    @Past(message = "La fecha de nacimiento debe estar en el pasado")
    private LocalDate fechaNacimiento;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email no tiene un formato válido")
    @Size(
            max = 255,
            message = "El email no puede superar 255 caracteres"
    )
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(
            min = 8,
            max = 64,
            message = "La contraseña debe tener entre 8 y 64 caracteres"
    )
    private String password;
}