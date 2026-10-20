package com.example.practica.controller;

import com.example.practica.dto.PostaliaResponse;
import com.example.practica.service.PostaliaService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Códigos postales", description = "Documentación de endpoints de códigos postales")
@RestController
@RequestMapping("/api/postalia")
public class PostaliaController {

    private final PostaliaService postaliaService;


    public PostaliaController(
            PostaliaService postaliaService
    ) {

        this.postaliaService =
                postaliaService;
    }


    @Operation(summary = "Consultar código postal", description = "Consulta la información de un código postal mexicano de cinco dígitos.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Información del código postal obtenida"),
            @ApiResponse(responseCode = "400", description = "El código postal no contiene exactamente cinco dígitos"),
            @ApiResponse(responseCode = "404", description = "Código postal no encontrado"),
            @ApiResponse(responseCode = "503", description = "Servicio de códigos postales no disponible"),
            @ApiResponse(responseCode = "500", description = "Error inesperado del servidor")
    })
    @GetMapping("/{codigoPostal}")
    public ResponseEntity<PostaliaResponse>
    consultarCodigoPostal(
            @Parameter(description = "Código postal mexicano de cinco dígitos", example = "42000") @PathVariable String codigoPostal
    ) {

        if (!codigoPostal.matches("\\d{5}")) {

            return ResponseEntity
                    .badRequest()
                    .build();
        }
        PostaliaResponse respuesta =
                postaliaService
                        .consultarCodigoPostal(
                                codigoPostal
                        );

        return ResponseEntity.ok(respuesta
        );
    }
}