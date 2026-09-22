package com.example.practica.controller;

import com.example.practica.dto.LoginRequest;
import com.example.practica.dto.LoginResponse;
import com.example.practica.service.AuthService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.web.csrf.CsrfToken;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;


    // =====================================================
    // LOGIN
    // =====================================================

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {

        // 1. Validar usuario y generar JWT
        LoginResponse response =
                authService.login(request);


        // 2. Obtener JWT generado por el Service
        String token =
                response.getToken();


        // 3. Crear cookie HttpOnly
        ResponseCookie cookie =
                ResponseCookie.from(
                                "jwt",
                                token
                        )

                        // JavaScript no puede leerla
                        .httpOnly(true)

                        /*
                         * IMPORTANTE:
                         *
                         * En desarrollo normalmente usamos:
                         * http://localhost:4200
                         * http://localhost:8081
                         *
                         * Por eso temporalmente usamos false.
                         *
                         * En producción con HTTPS debe ser true.
                         */
                        .secure(false)

                        // Angular y Spring están en localhost
                        .sameSite("Lax")

                        // Disponible para toda la API
                        .path("/")

                        // 1 hora
                        .maxAge(60 * 60)

                        .build();


        // 4. Enviar cookie + JSON SIN JWT
        return ResponseEntity
                .ok()
                .header(
                        HttpHeaders.SET_COOKIE,
                        cookie.toString()
                )
                .body(response);
    }


    // =====================================================
    // LOGOUT
    // =====================================================

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {

        /*
         * Se crea una cookie con el mismo nombre,
         * pero con duración 0.
         *
         * El navegador elimina la cookie.
         */
        ResponseCookie cookie =
                ResponseCookie.from(
                                "jwt",
                                ""
                        )
                        .httpOnly(true)
                        .secure(false)
                        .sameSite("Lax")
                        .path("/")
                        .maxAge(0)
                        .build();


        return ResponseEntity
                .ok()
                .header(
                        HttpHeaders.SET_COOKIE,
                        cookie.toString()
                )
                .build();
    }
    // =====================================================
// OBTENER TOKEN CSRF
// =====================================================

    @GetMapping("/csrf")
    public ResponseEntity<Void> csrf(
            CsrfToken csrfToken
    ) {

        // Fuerza la creación del token
        csrfToken.getToken();

        return ResponseEntity.ok().build();
    }
}