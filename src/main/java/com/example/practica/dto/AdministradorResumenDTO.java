package com.example.practica.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AdministradorResumenDTO {
    Long id;
    String nombre;
    String primerApellido;
    String correo;
}