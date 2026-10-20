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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.web.csrf.CsrfToken;

@Tag(name = "Autenticación y seguridad", description = "Documentación de endpoints de autenticación y seguridad")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;


    // =====================================================
    // LOGIN
    // =====================================================

    @Operation(summary = "Iniciar sesión", description = "Valida las credenciales y establece una cookie JWT HttpOnly.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Inicio de sesión correcto; cookie JWT HttpOnly enviada"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "500", description = "Error inesperado del servidor")
    })
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

    @Operation(summary = "Cerrar sesión", description = "Envía una cookie JWT con duración cero para cerrar la sesión en el navegador.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cookie JWT invalidada en el navegador"),
            @ApiResponse(responseCode = "403", description = "Solicitud rechazada por protección CSRF"),
            @ApiResponse(responseCode = "500", description = "Error inesperado del servidor")
    })
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

    @Operation(summary = "Obtener token CSRF", description = "Solicita la generación del token CSRF utilizado para proteger operaciones que modifican datos.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Token CSRF inicializado"),
            @ApiResponse(responseCode = "500", description = "Error inesperado del servidor")
    })
    @GetMapping("/csrf")
    public ResponseEntity<Void> csrf(
            @Parameter(hidden = true) CsrfToken csrfToken
    ) {

        // Fuerza la creación del token
        csrfToken.getToken();

        return ResponseEntity.ok().build();
    }
}