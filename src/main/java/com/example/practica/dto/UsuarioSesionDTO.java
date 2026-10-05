package com.example.practica.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UsuarioSesionDTO {
    Long id;
    String nombre;
    String primerApellido;
    String email;
    String rol;
}