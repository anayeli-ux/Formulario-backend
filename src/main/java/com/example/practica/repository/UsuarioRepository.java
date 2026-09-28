package com.example.practica.repository;

import com.example.practica.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository
        extends JpaRepository<Usuario, Long> {

    List<Usuario> findByFechaBajaIsNullOrderByIdAsc();

    List<Usuario> findByFechaBajaIsNotNullOrderByIdAsc();

    Optional<Usuario> findByIdAndFechaBajaIsNull(Long id);
}