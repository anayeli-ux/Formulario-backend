package com.example.practica.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Colonia asociada a un código postal")
public class ColoniaResponse {

    @Schema(description = "Nombre", example = "Ana")
    private String nombre;

    public ColoniaResponse() {
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}