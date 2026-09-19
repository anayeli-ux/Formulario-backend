package com.example.practica.repository;

import com.example.practica.model.Usuario;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository
        extends JpaRepository<Usuario, Long> {

    List<Usuario> findByActivoTrueOrderByIdAsc();

    List<Usuario> findByActivoFalseOrderByIdAsc();

    Optional<Usuario> findByIdAndActivoTrue(Long id);

    @EntityGraph(attributePaths = "rol")
    Optional<Usuario> findByEmail(String email);

    Optional<Usuario> findByEmailAndActivoTrue(String email);

    boolean existsByEmail(String email);
}