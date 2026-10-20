package com.example.practica.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Resultado del inicio de sesión")
public class LoginResponse {

    @Schema(description = "Indica si el inicio de sesión fue exitoso")
    private boolean acceso;

    /*
     * Se utiliza internamente entre el Service
     * y el Controller.
     *
     * @JsonIgnore evita que el JWT aparezca
     * en la respuesta JSON enviada a Angular.
     */
    @JsonIgnore
    private String token;

    private UsuarioLoginDTO usuario;
}