package com.example.practica.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TelefonoResponseDTO {

    private Long id;
    private String tipo;
    private String valor;
}
