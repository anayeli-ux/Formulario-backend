package com.example.practica.repository;

import com.example.practica.model.Usuario;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository
        extends JpaRepository<Usuario, Long> {

    // Usuarios activos:
    // fecha_baja IS NULL
    List<Usuario> findByFechaBajaIsNullOrderByIdAsc();

    // Usuarios dados de baja:
    // fecha_baja IS NOT NULL
    List<Usuario> findByFechaBajaIsNotNullOrderByIdAsc();

    // Buscar un usuario activo por ID
    Optional<Usuario> findByIdAndFechaBajaIsNull(Long id);

    // Buscar por email incluyendo su rol.
    // Puede encontrar activos o dados de baja.
    @EntityGraph(attributePaths = "rol")
    Optional<Usuario> findByEmail(String email);

    // Buscar por email solamente si está activo
    @EntityGraph(attributePaths = "rol")
    Optional<Usuario> findByEmailAndFechaBajaIsNull(String email);

    // Verificar si el email ya existe
    boolean existsByEmail(String email);
}