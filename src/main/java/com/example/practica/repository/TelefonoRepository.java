package com.example.practica.repository;

import com.example.practica.model.Telefono;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TelefonoRepository
        extends JpaRepository<Telefono, Long> {

    List<Telefono> findByUsuarioId(Long usuarioId);

    boolean existsByTelefono(String telefono);
}