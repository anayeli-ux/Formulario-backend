package com.example.practica.exception;

import com.example.practica.dto.ApiResult;
import com.example.practica.factory.ApiResponseFactory;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // =====================================================
    // ERRORES DE @Valid -> 400
    // =====================================================

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResult<Object>> handleValidationErrors(
            MethodArgumentNotValidException ex
    ) {

        // Almacena el nombre del campo y su mensaje de error.
        Map<String, String> camposConError = new HashMap<>();

        for (FieldError error :
                ex.getBindingResult().getFieldErrors()) {

            camposConError.put(
                    error.getField(),
                    error.getDefaultMessage()
            );
        }

        return ResponseEntity
                .badRequest()
                .body(
                        ApiResponseFactory.error(
                                "Datos inválidos",
                                camposConError
                        )
                );
    }

    // =====================================================
    // USUARIO NO ENCONTRADO -> 404
    // =====================================================

    @ExceptionHandler(UsuarioNoEncontradoException.class)
    public ResponseEntity<ApiResult<Object>> handleUsuarioNoEncontrado(
            UsuarioNoEncontradoException ex
    ) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(
                        ApiResponseFactory.error(
                                ex.getMessage(),
                                null
                        )
                );
    }

    // =====================================================
    // CÓDIGO POSTAL NO ENCONTRADO -> 404
    // =====================================================

    @ExceptionHandler(CodigoPostalNoEncontradoException.class)
    public ResponseEntity<ApiResult<Object>> handleCodigoPostalNoEncontrado(
            CodigoPostalNoEncontradoException ex
    ) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(
                        ApiResponseFactory.error(
                                ex.getMessage(),
                                null
                        )
                );
    }

    // =====================================================
    // POSTALIA NO DISPONIBLE -> 503
    // =====================================================

    @ExceptionHandler(PostaliaNoDisponibleException.class)
    public ResponseEntity<ApiResult<Object>> handlePostaliaNoDisponible(
            PostaliaNoDisponibleException ex
    ) {

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(
                        ApiResponseFactory.error(
                                ex.getMessage(),
                                null
                        )
                );
    }

    // =====================================================
    // TELÉFONO DUPLICADO -> 409
    // =====================================================

    @ExceptionHandler(TelefonoDuplicadoException.class)
    public ResponseEntity<ApiResult<Object>> handleTelefonoDuplicado(
            TelefonoDuplicadoException ex
    ) {

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(
                        ApiResponseFactory.error(
                                ex.getMessage(),
                                null
                        )
                );
    }

    // =====================================================
    // EMAIL DUPLICADO -> 409
    // =====================================================

    @ExceptionHandler(EmailDuplicadoException.class)
    public ResponseEntity<ApiResult<Object>> handleEmailDuplicado(
            EmailDuplicadoException ex
    ) {

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(
                        ApiResponseFactory.error(
                                ex.getMessage(),
                                null
                        )
                );
    }

    // =====================================================
    // MENOR DE EDAD -> 400
    // =====================================================

    @ExceptionHandler(MenorDeEdadException.class)
    public ResponseEntity<ApiResult<Object>> handleMenorDeEdad(
            MenorDeEdadException ex
    ) {

        return ResponseEntity
                .badRequest()
                .body(
                        ApiResponseFactory.error(
                                ex.getMessage(),
                                null
                        )
                );
    }

    // =====================================================
    // INTENTAR REACTIVAR USUARIO ACTIVO -> 409
    // =====================================================

    @ExceptionHandler(UsuarioYaActivoException.class)
    public ResponseEntity<ApiResult<Object>> handleUsuarioYaActivo(
            UsuarioYaActivoException ex
    ) {

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(
                        ApiResponseFactory.error(
                                ex.getMessage(),
                                null
                        )
                );
    }

    // =====================================================
    // ERROR GENERAL -> 500
    // =====================================================

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResult<Object>> handleGeneral(
            Exception ex
    ) {

        // Permite identificar el error real en la consola.
        ex.printStackTrace();

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(
                        ApiResponseFactory.error(
                                "Ocurrió un error inesperado",
                                null
                        )
                );
    }
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResult<Object>> handleIllegalArgument(
            IllegalArgumentException ex
    ) {
        return ResponseEntity
                .badRequest()
                .body(
                        ApiResponseFactory.error(
                                ex.getMessage(),
                                null
                        )
                );
    }
}