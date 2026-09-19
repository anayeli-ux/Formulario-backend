package com.example.practica.exception;

public class UsuarioNoEncontradoException extends RuntimeException {

    public UsuarioNoEncontradoException(Long id) {
        super("No se encontró un usuario con id: " + id);
    }

    public UsuarioNoEncontradoException(String email) {
        super("No se encontró un usuario con email: " + email);
    }
}