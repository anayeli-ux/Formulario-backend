package com.example.practica.repository;

import com.example.practica.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository
        extends JpaRepository<Usuario, Long> {

    // Solo usuarios activos
    List<Usuario> findByActivoTrueOrderByIdAsc();

    // Solo usuarios eliminados lógicamente
    List<Usuario> findByActivoFalseOrderByIdAsc();

    // Buscar solamente si está activo
    Optional<Usuario> findByIdAndActivoTrue(Long id);

    // Validar que el teléfono no esté repetido
    boolean existsByTelefono(String telefono);
}