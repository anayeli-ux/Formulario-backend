package com.example.practica.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ContactoVisibleDTO {
    String tipo;
    String valor;
    String codigoPostal;
}