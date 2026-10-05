package com.example.practica.dto;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class UsuarioContactosDTO {
    Long id;
    String nombre;
    String primerApellido;
    List<ContactoVisibleDTO> telefonos;
    List<ContactoVisibleDTO> correos;
    List<ContactoVisibleDTO> direcciones;
}