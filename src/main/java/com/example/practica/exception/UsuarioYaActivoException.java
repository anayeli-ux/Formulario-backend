package com.example.practica.exception;

public class UsuarioYaActivoException extends RuntimeException {

    public UsuarioYaActivoException(Long id) {
        super("El usuario con id " + id + " ya se encuentra activo");
    }
}