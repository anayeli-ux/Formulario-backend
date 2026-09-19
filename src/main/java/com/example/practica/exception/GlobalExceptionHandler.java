package com.example.practica.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // =====================================================
    // ERRORES DE @Valid -> 400
    // =====================================================

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>>
    handleValidationErrors(
            MethodArgumentNotValidException ex
    ) {

        Map<String, String> camposConError =
                new HashMap<>();

        for (FieldError error :
                ex.getBindingResult().getFieldErrors()) {

            camposConError.put(
                    error.getField(),
                    error.getDefaultMessage()
            );
        }

        Map<String, Object> body =
                errorBase(
                        HttpStatus.BAD_REQUEST,
                        "Datos inválidos"
                );

        body.put(
                "errores",
                camposConError
        );

        return ResponseEntity
                .badRequest()
                .body(body);
    }


    // =====================================================
    // USUARIO NO ENCONTRADO -> 404
    // =====================================================

    @ExceptionHandler(UsuarioNoEncontradoException.class)
    public ResponseEntity<Map<String, Object>>
    handleUsuarioNoEncontrado(
            UsuarioNoEncontradoException ex
    ) {

        Map<String, Object> body =
                errorBase(
                        HttpStatus.NOT_FOUND,
                        ex.getMessage()
                );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(body);
    }


    // =====================================================
    // CÓDIGO POSTAL NO ENCONTRADO -> 404
    // =====================================================

    @ExceptionHandler(CodigoPostalNoEncontradoException.class)
    public ResponseEntity<Map<String, Object>>
    handleCodigoPostalNoEncontrado(
            CodigoPostalNoEncontradoException ex
    ) {

        Map<String, Object> body =
                errorBase(
                        HttpStatus.NOT_FOUND,
                        ex.getMessage()
                );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(body);
    }


    // =====================================================
    // POSTALIA NO DISPONIBLE -> 503
    // =====================================================

    @ExceptionHandler(PostaliaNoDisponibleException.class)
    public ResponseEntity<Map<String, Object>>
    handlePostaliaNoDisponible(
            PostaliaNoDisponibleException ex
    ) {

        Map<String, Object> body =
                errorBase(
                        HttpStatus.SERVICE_UNAVAILABLE,
                        ex.getMessage()
                );

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(body);
    }


    // =====================================================
    // TELÉFONO DUPLICADO -> 409
    // =====================================================

    @ExceptionHandler(TelefonoDuplicadoException.class)
    public ResponseEntity<Map<String, Object>>
    handleTelefonoDuplicado(
            TelefonoDuplicadoException ex
    ) {

        Map<String, Object> body =
                errorBase(
                        HttpStatus.CONFLICT,
                        ex.getMessage()
                );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(body);
    }


    // =====================================================
    // EMAIL DUPLICADO -> 409
    // =====================================================

    @ExceptionHandler(EmailDuplicadoException.class)
    public ResponseEntity<Map<String, Object>>
    handleEmailDuplicado(
            EmailDuplicadoException ex
    ) {

        Map<String, Object> body =
                errorBase(
                        HttpStatus.CONFLICT,
                        ex.getMessage()
                );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(body);
    }


    // =====================================================
    // MENOR DE EDAD -> 400
    // =====================================================

    @ExceptionHandler(MenorDeEdadException.class)
    public ResponseEntity<Map<String, Object>>
    handleMenorDeEdad(
            MenorDeEdadException ex
    ) {

        Map<String, Object> body =
                errorBase(
                        HttpStatus.BAD_REQUEST,
                        ex.getMessage()
                );

        return ResponseEntity
                .badRequest()
                .body(body);
    }


    // =====================================================
    // INTENTAR REACTIVAR USUARIO ACTIVO -> 409
    // =====================================================

    @ExceptionHandler(UsuarioYaActivoException.class)
    public ResponseEntity<Map<String, Object>>
    handleUsuarioYaActivo(
            UsuarioYaActivoException ex
    ) {

        Map<String, Object> body =
                errorBase(
                        HttpStatus.CONFLICT,
                        ex.getMessage()
                );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(body);
    }


    // =====================================================
    // ERROR GENERAL -> 500
    // =====================================================

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>>
    handleGeneral(
            Exception ex
    ) {

        /*
         * Lo mostramos en consola para poder encontrar
         * el error real durante desarrollo.
         */
        ex.printStackTrace();

        Map<String, Object> body =
                errorBase(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Ocurrió un error inesperado"
                );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(body);
    }


    // =====================================================
    // ESTRUCTURA COMÚN
    // =====================================================

    private Map<String, Object> errorBase(
            HttpStatus status,
            String mensaje
    ) {

        Map<String, Object> body =
                new HashMap<>();

        body.put(
                "timestamp",
                LocalDateTime.now()
        );

        body.put(
                "status",
                status.value()
        );

        body.put(
                "mensaje",
                mensaje
        );

        return body;
    }
}