package com.example.practica.dto;

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
public class LoginResponse {

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