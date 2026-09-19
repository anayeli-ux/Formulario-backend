package com.example.practica.repository;

import com.example.practica.model.CodigoPostal;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CodigoPostalRepository
        extends JpaRepository<CodigoPostal, String> {
}