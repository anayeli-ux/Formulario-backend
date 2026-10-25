package com.example.practica.factory;

import com.example.practica.dto.ApiResult;

public final class ApiResponseFactory {

    private ApiResponseFactory() {
        // Evita crear instancias de esta clase.
    }

    public static <T> ApiResult<T> success(
            String message,
            T data
    ) {
        return new ApiResult<>(
                true,
                message,
                data,
                null
        );
    }

    public static <T> ApiResult<T> error(
            String message,
            Object errors
    ) {
        return new ApiResult<>(
                false,
                message,
                null,
                errors
        );
    }
}