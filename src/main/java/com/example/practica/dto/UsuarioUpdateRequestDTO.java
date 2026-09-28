package com.example.practica.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioUpdateRequestDTO {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(
            min = 2,
            max = 255,
            message = "El nombre debe tener entre 2 y 255 caracteres"
    )
    private String nombre;

    @NotBlank(message = "El primer apellido es obligatorio")
    @Size(
            max = 255,
            message = "El primer apellido no puede superar los 255 caracteres"
    )
    private String primerApellido;

    /*
     * null o "" = conservar la contraseña actual.
     */
    private String password;

    @NotNull(message = "La fecha de nacimiento es obligatoria")
    @Past(
            message = "La fecha de nacimiento debe ser anterior a la fecha actual"
    )
    private LocalDate fechaNacimiento;

    @NotEmpty(message = "Debe registrar al menos un teléfono")
    @Valid
    @Builder.Default
    private List<TelefonoRequest> telefonos =
            new ArrayList<>();

    @NotEmpty(message = "Debe registrar al menos un correo")
    @Valid
    @Builder.Default
    private List<CorreoRequest> correos =
            new ArrayList<>();

    @NotEmpty(message = "Debe registrar al menos una dirección")
    @Valid
    @Builder.Default
    private List<DireccionRequest> direcciones =
            new ArrayList<>();
}