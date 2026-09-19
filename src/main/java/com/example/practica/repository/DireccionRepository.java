package com.example.practica.repository;

import com.example.practica.model.Direccion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DireccionRepository extends JpaRepository<Direccion, Long> {

    List<Direccion> findByUsuarioId(Long usuarioId);
}