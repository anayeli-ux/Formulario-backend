package com.example.practica.controller;

import com.example.practica.dto.LoginRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @PostMapping("/admin")
    public ResponseEntity<Map<String, Boolean>>
    loginAdmin(
            @RequestBody LoginRequest datos
    ) {

        boolean acceso =
                "admin".equals(datos.getUsuario())
                        &&
                        "12356".equals(datos.getPassword());

        return ResponseEntity.ok(
                Map.of("acceso", acceso)
        );
    }
}