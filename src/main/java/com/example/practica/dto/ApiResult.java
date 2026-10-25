package com.example.practica.dto;

public record ApiResult<T>(
        boolean success,
        String message,
        T data,
        Object errors
) {
}