package com.example.practica.dto;

import io.swagger.v3.oas.annotations.media.Schema;

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
@Schema(description = "Datos necesarios para registrar un usuario")
public class UsuarioRequestDTO {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(
            min = 2,
            max = 255,
            message = "El nombre debe tener entre 2 y 255 caracteres"
    )
    @Schema(description = "Nombre", example = "Ana")
    private String nombre;

    @NotBlank(message = "El primer apellido es obligatorio")
    @Size(
            max = 255,
            message = "El primer apellido no puede superar los 255 caracteres"
    )
    @Schema(description = "Primer apellido", example = "Garcia")
    private String primerApellido;

        @Pattern(regexp = "USER|ADMIN", message = "El rol debe ser USER o ADMIN")
        @Schema(description = "Rol del usuario: USER o ADMIN", allowableValues = {"USER", "ADMIN"})
        private String rol;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(
            min = 8,
            max = 255,
            message = "La contraseña debe tener al menos 8 caracteres"
    )
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z\\d]).{8,}$",
            message = "La contraseña debe contener al menos una letra, un número y un símbolo"
    )
    @Schema(description = "Contraseña para autenticación", format = "password", accessMode = Schema.AccessMode.WRITE_ONLY)
    private String password;

    @NotNull(message = "La fecha de nacimiento es obligatoria")
    @Past(
            message = "La fecha de nacimiento debe ser anterior a la fecha actual"
    )
    @Schema(description = "Fecha de nacimiento (anterior a la fecha actual)", example = "2000-10-20")
    private LocalDate fechaNacimiento;

    @NotEmpty(message = "Debe registrar al menos un teléfono")
    @Builder.Default
    @Schema(description = "Teléfonos asociados al usuario")
    private List<@Valid TelefonoRequest> telefonos =
            new ArrayList<>();

    @NotEmpty(message = "Debe registrar al menos un correo")
    @Builder.Default
    @Schema(description = "Correos electrónicos asociados al usuario")
    private List<@Valid CorreoRequest> correos =
            new ArrayList<>();

    @NotEmpty(message = "Debe registrar al menos una dirección")
    @Builder.Default
    @Schema(description = "Direcciones asociadas al usuario")
    private List<@Valid DireccionRequest> direcciones =
            new ArrayList<>();
}