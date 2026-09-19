package com.example.practica.service;

import com.example.practica.dto.LoginRequest;
import com.example.practica.dto.LoginResponse;

public interface AuthService {

    LoginResponse login(
            LoginRequest request
    );
}