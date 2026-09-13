package com.example.practica.exception;

public class MenorDeEdadException extends RuntimeException {

    public MenorDeEdadException() {
        super("El usuario debe tener al menos 18 años");
    }
}