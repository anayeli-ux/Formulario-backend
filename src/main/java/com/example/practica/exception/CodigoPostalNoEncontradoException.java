package com.example.practica.exception;

public class CodigoPostalNoEncontradoException extends RuntimeException {
    public CodigoPostalNoEncontradoException(String codigoPostal) {
        super("No se encontró información para el código postal: " + codigoPostal);
    }
}