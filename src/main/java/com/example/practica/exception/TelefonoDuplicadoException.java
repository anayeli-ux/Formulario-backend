package com.example.practica.exception;

public class TelefonoDuplicadoException extends RuntimeException {

    public TelefonoDuplicadoException() {
        super("Ya existe un usuario registrado con ese número de teléfono");
    }
}