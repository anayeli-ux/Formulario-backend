package com.example.practica.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import org.springframework.data.domain.Page;

@Schema(description = "Resultado paginado de una consulta")
public record PageResponse<T>(
        @Schema(description = "Elementos de la página") List<T> content,
        @Schema(description = "Cantidad total de elementos") long totalElements,
        @Schema(description = "Cantidad total de páginas") int totalPages
) {
    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}