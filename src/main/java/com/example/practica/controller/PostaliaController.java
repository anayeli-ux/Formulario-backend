package com.example.practica.controller;

import com.example.practica.dto.PostaliaResponse;
import com.example.practica.service.PostaliaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/postalia")
@CrossOrigin(origins = "http://localhost:4200")
public class PostaliaController {

    private final PostaliaService postaliaService;

    public PostaliaController(PostaliaService postaliaService) {
        this.postaliaService = postaliaService;
    }

    @GetMapping("/{codigoPostal}")
    public ResponseEntity<PostaliaResponse> consultarCodigoPostal(
            @PathVariable String codigoPostal) {

        if (!codigoPostal.matches("\\d{5}")) {
            return ResponseEntity.badRequest().build();
        }

        PostaliaResponse respuesta =
                postaliaService.consultarCodigoPostal(codigoPostal);

        return ResponseEntity.ok(respuesta);
    }
}