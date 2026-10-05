package com.example.practica.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UsuarioResumenDTO {
    Long id;
    String nombre;
    String primerApellido;
    String rol;
    String telefono;
    String correo;
    String codigoPostal;
}