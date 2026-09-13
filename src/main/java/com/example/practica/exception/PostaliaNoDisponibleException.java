package com.example.practica.exception;

public class PostaliaNoDisponibleException extends RuntimeException {
    public PostaliaNoDisponibleException() {
        super("El servicio de códigos postales no está disponible en este momento. Intenta de nuevo más tarde.");
    }
}