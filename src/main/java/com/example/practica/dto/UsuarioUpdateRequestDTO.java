package com.example.practica.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
    @Size(min = 2, max = 255, message = "El nombre debe tener entre 2 y 255 caracteres")
    private String nombre;

    @NotBlank(message = "El primer apellido es obligatorio")
    @Size(max = 255, message = "El primer apellido no puede superar los 255 caracteres")
    private String primerApellido;


    /*
     * IMPORTANTE:
     * En actualización la contraseña NO es obligatoria.
     *
     * null o "" = conservar contraseña actual.
     */
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
    @Size(
            max = 255,
            message = "La dirección no puede superar los 255 caracteres"
    )
    private String direccion;

    @Past(message = "La fecha de nacimiento debe ser anterior a la fecha actual")
    private LocalDate fechaNacimiento;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo no tiene un formato válido")
    @Size(max = 255, message = "El correo no puede superar los 255 caracteres")
    private String email;

    @Valid
    @Builder.Default
    private List<TelefonoRequest> telefonos = new ArrayList<>();

    @Valid
    @Builder.Default
    private List<DireccionRequest> direcciones = new ArrayList<>();
}